package br.com.robsonkades.worker.outbox;

import br.com.robsonkades.worker.util.InstantUtils;
import br.com.robsonkades.worker.transaction.Transaction;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class OutboxWorker implements SmartLifecycle {

    private final OutboxJpaRepository outboxRepo;
    private final Transaction transaction;

    private boolean isRunning = true;

    //@Scheduled(cron = "*/1 * 16-23 * * *", zone = "America/Sao_Paulo")
    public OutboxWorker(OutboxJpaRepository outboxRepo, Transaction transaction) {
        this.outboxRepo = outboxRepo;
        this.transaction = transaction;
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 100, scheduler = "fastScheduler")
    public void processOrderCreated() {
        executeJob(OutboxEventType.ORDER_CREATED, OutboxEventType.ORDER_SHIPPED);
    }

    private void executeJob(OutboxEventType currentEvent, OutboxEventType nextEvent) {
        final AtomicReference<KeysetScrollPosition> keyset = new AtomicReference<>(ScrollPosition.keyset());

        boolean pending;
        int maxRounds = 20;

        do {
            //noinspection ConstantConditions
            pending = transaction.execute(() -> {
                Window<OutboxJpaEntity> window = outboxRepo.findFirst100ByEventTypeAndStatusOrderByIdAsc(currentEvent, OutboxStatus.PENDING, keyset.get());
                List<OutboxJpaEntity> content = window.getContent();

                if (content.isEmpty()) {
                    return true;
                }

                Instant now = InstantUtils.now();
                List<OutboxJpaEntity> newEvents = new ArrayList<>(content.size());

                for (OutboxJpaEntity current : content) {
                    current.setStatus(OutboxStatus.PROCESSED);
                    current.setProcessedAt(InstantUtils.now());


                    if (nextEvent != null) {
                        OutboxJpaEntity outboxEvent = new OutboxJpaEntity();
                        outboxEvent.setEventType(nextEvent);
                        outboxEvent.setAggregateId(current.getAggregateId());
                        outboxEvent.setAggregateType("Order");
                        outboxEvent.setPayload(current.getPayload());
                        outboxEvent.setStatus(OutboxStatus.PENDING);
                        outboxEvent.setCreatedAt(now);
                        newEvents.add(outboxEvent);
                    }
                }

                outboxRepo.saveAll(content);
                outboxRepo.saveAll(newEvents);

                if (window.hasNext()) {
                    KeysetScrollPosition position = (KeysetScrollPosition) window.positionAt(window.size() - 1);
                    keyset.set(position);
                }

                return true;
            });
        } while (isRunning && pending && --maxRounds > 0);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 100, scheduler = "fastScheduler")
    public void processOrderShipped() {
        executeJob(OutboxEventType.ORDER_SHIPPED, OutboxEventType.PAYMENT_CONFIRMED);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 100, scheduler = "fastScheduler")
    public void processPaymentConfirmed() {
        executeJob(OutboxEventType.PAYMENT_CONFIRMED, OutboxEventType.INVOICE_GENERATED);
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 100, scheduler = "fastScheduler")
    public void processInvoiceGenerated() {
        executeJob(OutboxEventType.INVOICE_GENERATED, null);
    }

    @Override
    public void start() {
        this.isRunning = true;
    }

    @Override
    public void stop() {
        this.isRunning = false;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning;
    }
}
