package br.com.robsonkades.worker.outbox;

import br.com.robsonkades.worker.util.InstantUtils;
import io.github.robsonkades.uuidv7.UUIDv7;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RequestMapping("/outbox")
@RestController
public class OutboxController {

    public final OutboxJpaRepository outboxJpaRepository;

    public OutboxController(OutboxJpaRepository outboxJpaRepository) {
        this.outboxJpaRepository = outboxJpaRepository;
    }

    @PostMapping
    @Transactional
    public void create() {
        OutboxJpaEntity outboxJpaEntity = new OutboxJpaEntity();
        outboxJpaEntity.setPayload("payload");
        outboxJpaEntity.setCreatedAt(InstantUtils.now());
        outboxJpaEntity.setStatus(OutboxStatus.PENDING);
        outboxJpaEntity.setEventType(OutboxEventType.ORDER_CREATED);
        outboxJpaEntity.setAggregateId("k6");
        outboxJpaEntity.setAggregateType("order");
        outboxJpaEntity.setAggregateId(UUIDv7.randomUUID().toString());
        outboxJpaRepository.save(outboxJpaEntity);
    }

    @GetMapping
    @Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
    public OutboxResponseDTO keySet(@RequestParam(required = false) Map<String, Object> next) {
        ScrollPosition position;
        if (next == null || next.isEmpty()) {
            position = ScrollPosition.keyset();
        } else {
            position = ScrollPosition.of(next, ScrollPosition.Direction.FORWARD);
        }

        return new OutboxResponseDTO(outboxJpaRepository.findFirst50ByOrderByIdDesc(position));
    }

    public static class OutboxResponseDTO {

        private final List<OutboxJpaEntity> content;
        private final Map<String, Object> next;
        private final boolean hasNext;

        public OutboxResponseDTO(Window<OutboxJpaEntity> window) {
            this.content = window.getContent();
            this.hasNext = window.hasNext();

            if (this.hasNext) {
                KeysetScrollPosition position = (KeysetScrollPosition) window.positionAt(window.size() - 1);
                this.next = position.getKeys();
            } else {
                this.next = null;
            }
        }

        public List<OutboxJpaEntity> getContent() {
            return content;
        }

        public Map<String, Object> getNext() {
            return next;
        }

        public boolean hasNext() {
            return hasNext;
        }
    }
}
