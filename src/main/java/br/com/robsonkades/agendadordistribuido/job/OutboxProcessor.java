package br.com.robsonkades.agendadordistribuido.job;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class OutboxProcessor implements SmartLifecycle {

    private final OutboxRepository outboxRepo;
    private final TransactionTemplate transactionManager;
    private final MeterRegistry meterRegistry;

    private final AtomicLong orderCreatedId = new AtomicLong(0);
    private final AtomicLong paymentConfirmedId = new AtomicLong(0);
    private final AtomicLong InvoiceGeneratedId = new AtomicLong(0);

    private boolean isRunning = true;

    //@Scheduled(cron = "*/1 * 16-23 * * *", zone = "America/Sao_Paulo")
    public OutboxProcessor(OutboxRepository outboxRepo, TransactionTemplate transactionManager, MeterRegistry meterRegistry) {
        this.outboxRepo = outboxRepo;
        this.transactionManager = transactionManager;
        this.meterRegistry = meterRegistry;
    }

    @Scheduled(fixedDelay = 2000)
    public void processOrderCreated() {
        meterRegistry.counter("outbox.executed", "name", "processOrderCreated").count();

        boolean pending = true;

        Map<String, Object> firtElement = new HashMap<>();
        firtElement.put("id", orderCreatedId.get());

        final AtomicReference<KeysetScrollPosition> keyset = new AtomicReference<>(ScrollPosition.of(firtElement, ScrollPosition.Direction.FORWARD));

        while (isRunning() && pending) {
            //noinspection ConstantConditions
            pending = transactionManager.execute(transactionStatus -> {

                Window<OutboxEvent> window = outboxRepo.findFirst50ByEventTypeAndStatusOrderByIdAsc(EventType.OrderCreated, Status.PENDING, keyset.get());

                if (window.isEmpty()) {
                    return false;
                }

                if (window.hasNext()) {
                    KeysetScrollPosition position = (KeysetScrollPosition) window.positionAt(window.size() - 1);
                    keyset.set(position);
                    orderCreatedId.set(window.size() - 1);
                }

                List<OutboxEvent> outboxEvents = window.getContent();

                Instant now = InstantUtils.now();
                List<OutboxEvent> newEvents = new ArrayList<>();

                for (OutboxEvent proposal : outboxEvents) {
                    // Atualiza o status da proposta
                    proposal.setStatus(Status.PROCESSED);
                    proposal.setProcessedAt(now);

                    // Cria novo evento para ser salvo depois
                    OutboxEvent outboxEvent = new OutboxEvent();
                    outboxEvent.setEventType(EventType.OrderShipped);
                    outboxEvent.setAggregateId(proposal.getAggregateId());
                    outboxEvent.setAggregateType("Order");
                    outboxEvent.setPayload(proposal.getPayload());
                    outboxEvent.setStatus(Status.PENDING);
                    outboxEvent.setCreatedAt(now);

                    newEvents.add(outboxEvent);
                }

                outboxRepo.saveAll(outboxEvents);
                outboxRepo.saveAll(newEvents);
                return true;

            });
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void processOrderShipped() {
        meterRegistry.counter("outbox.executed", "name", "processOrderShipped").increment();

        boolean pending = true;

        Map<String, Object> firtElement = new HashMap<>();
        firtElement.put("id", paymentConfirmedId.get());

        final AtomicReference<KeysetScrollPosition> keyset = new AtomicReference<>(ScrollPosition.of(firtElement, ScrollPosition.Direction.FORWARD));

        while (isRunning() && pending) {
            //noinspection ConstantConditions
            pending = transactionManager.execute(transactionStatus -> {

                Window<OutboxEvent> window = outboxRepo.findFirst50ByEventTypeAndStatusOrderByIdAsc(EventType.OrderShipped, Status.PENDING, keyset.get());

                if (window.isEmpty()) {
                    return false;
                }

                if (window.hasNext()) {
                    KeysetScrollPosition position = (KeysetScrollPosition) window.positionAt(window.size() - 1);
                    keyset.set(position);
                    paymentConfirmedId.set(window.size() - 1);
                }

                List<OutboxEvent> outboxEvents = window.getContent();

                Instant now = InstantUtils.now();
                List<OutboxEvent> newEvents = new ArrayList<>();

                for (OutboxEvent proposal : outboxEvents) {
                    // Atualiza o status da proposta
                    proposal.setStatus(Status.PROCESSED);
                    proposal.setProcessedAt(now);

                    // Cria novo evento para ser salvo depois
                    OutboxEvent outboxEvent = new OutboxEvent();
                    outboxEvent.setEventType(EventType.PaymentConfirmed);
                    outboxEvent.setAggregateId(proposal.getAggregateId());
                    outboxEvent.setAggregateType("Order");
                    outboxEvent.setPayload(proposal.getPayload());
                    outboxEvent.setStatus(Status.PENDING);
                    outboxEvent.setCreatedAt(now);

                    newEvents.add(outboxEvent);
                }

                outboxRepo.saveAll(outboxEvents);
                outboxRepo.saveAll(newEvents);
                return true;
            });
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void processPaymentConfirmed() {
        meterRegistry.counter("outbox.executed", "name", "processPaymentConfirmed").count();

        boolean pending = true;
        Map<String, Object> firtElement = new HashMap<>();
        firtElement.put("id", paymentConfirmedId.get());

        final AtomicReference<KeysetScrollPosition> keyset = new AtomicReference<>(ScrollPosition.of(firtElement, ScrollPosition.Direction.FORWARD));

        while (isRunning() && pending) {

            //noinspection ConstantConditions
            pending = transactionManager.execute(transactionStatus -> {

                Window<OutboxEvent> window = outboxRepo.findFirst50ByEventTypeAndStatusOrderByIdAsc(EventType.PaymentConfirmed, Status.PENDING, keyset.get());

                if (window.isEmpty()) {
                    return false;
                }

                if (window.hasNext()) {
                    KeysetScrollPosition position = (KeysetScrollPosition) window.positionAt(window.size() - 1);
                    keyset.set(position);
                    paymentConfirmedId.set(window.size() - 1);
                }

                List<OutboxEvent> outboxEvents = window.getContent();

                Instant now = InstantUtils.now();
                List<OutboxEvent> newEvents = new ArrayList<>();

                for (OutboxEvent proposal : outboxEvents) {
                    // Atualiza o status da proposta
                    proposal.setStatus(Status.PROCESSED);
                    proposal.setProcessedAt(now);

                    // Cria novo evento para ser salvo depois
                    OutboxEvent outboxEvent = new OutboxEvent();
                    outboxEvent.setEventType(EventType.InvoiceGenerated);
                    outboxEvent.setAggregateId(proposal.getAggregateId());
                    outboxEvent.setAggregateType("Order");
                    outboxEvent.setPayload(proposal.getPayload());
                    outboxEvent.setStatus(Status.PENDING);
                    outboxEvent.setCreatedAt(now);

                    newEvents.add(outboxEvent);
                }

                outboxRepo.saveAll(outboxEvents);
                outboxRepo.saveAll(newEvents);
                return true;
            });
        }
    }

    @Scheduled(fixedDelay = 2000)
    public void processInvoiceGenerated() {
        meterRegistry.counter("outbox.executed", "name", "processInvoiceGenerated").count();

        boolean pending = true;
        Map<String, Object> firtElement = new HashMap<>();
        firtElement.put("id", InvoiceGeneratedId.get());

        final AtomicReference<KeysetScrollPosition> keyset = new AtomicReference<>(ScrollPosition.of(firtElement, ScrollPosition.Direction.FORWARD));

        while (isRunning() && pending) {
            //noinspection ConstantConditions
            pending = transactionManager.execute(transactionStatus -> {

                Window<OutboxEvent> window = outboxRepo.findFirst50ByEventTypeAndStatusOrderByIdAsc(EventType.InvoiceGenerated, Status.PENDING, keyset.get());
                if (window.isEmpty()) {
                    return false;
                }

                if (window.hasNext()) {
                    KeysetScrollPosition position = (KeysetScrollPosition) window.positionAt(window.size() - 1);
                    keyset.set(position);
                    paymentConfirmedId.set(window.size() - 1);
                }

                List<OutboxEvent> outboxEvents = window.getContent();

                Instant now = InstantUtils.now();
                List<OutboxEvent> newEvents = new ArrayList<>();

                for (OutboxEvent proposal : outboxEvents) {
                    proposal.setStatus(Status.PROCESSED);
                    proposal.setProcessedAt(now);
                }

                outboxRepo.saveAll(outboxEvents);
                outboxRepo.saveAll(newEvents);
                return true;
            });
        }
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
