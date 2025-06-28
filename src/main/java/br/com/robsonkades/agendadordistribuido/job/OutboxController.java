package br.com.robsonkades.agendadordistribuido.job;


import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("/outbox")
@RestController
public class OutboxController {

    public final OutboxRepository outboxRepository;

    public OutboxController(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @GetMapping
    @Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
    public OutboxResponseDTO getProdutos(@RequestParam(required = false) Map<String, Object> next) {
        ScrollPosition position;
        if (next == null || next.isEmpty()) {
            position = ScrollPosition.keyset();
        } else {
            position = ScrollPosition.of(next, ScrollPosition.Direction.FORWARD);
        }

        return new OutboxResponseDTO(outboxRepository.findFirst50ByOrderByIdDesc(position));
    }


    public static class OutboxResponseDTO {

        private final List<OutboxEvent> content;
        private final Map<String, Object> next;
        private final boolean hasNext;

        public OutboxResponseDTO(Window<OutboxEvent> window) {
            this.content = window.getContent();
            this.hasNext = window.hasNext();

            if (this.hasNext) {
                KeysetScrollPosition position = (KeysetScrollPosition) window.positionAt(window.size() - 1);
                this.next = position.getKeys();
            } else {
                this.next = null;
            }
        }

        public List<OutboxEvent> getContent() {
            return content;
        }

        public Map<String, Object> getNext() {
            return next;
        }

        public boolean isHasNext() {
            return hasNext;
        }
    }
}
