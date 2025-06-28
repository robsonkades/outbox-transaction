package br.com.robsonkades.agendadordistribuido;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxProcessor {

    private final OutboxRepository outboxRepo;

    public OutboxProcessor(OutboxRepository outboxRepo) {
        this.outboxRepo = outboxRepo;
    }

    @Scheduled(fixedDelay = 1000)
    public void processOutbox() {
        List<OutboxEvent> events = outboxRepo.findTop500ByEventTypeAndStatus("DOCUMENTO_RECEBIDO", "PENDENTE");

        System.out.println("Processando " + events.size() + " eventos");
        events.forEach(event -> {
            try {

            } catch (Exception ex) {

            }
        });
    }
}
