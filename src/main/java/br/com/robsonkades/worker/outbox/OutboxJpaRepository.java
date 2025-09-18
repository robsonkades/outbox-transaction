package br.com.robsonkades.worker.outbox;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

public interface OutboxJpaRepository extends JpaRepository<OutboxJpaEntity, Long> {

    @Lock(LockModeType.NONE)
    Window<OutboxJpaEntity> findFirst50ByOrderByIdDesc(ScrollPosition position);

    @QueryHints({
            @QueryHint(
                    name = "jakarta.persistence.lock.timeout",
                    value = "-2"  //LockOptions.SKIP_LOCKED
            )
    })
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Window<OutboxJpaEntity> findFirst100ByEventTypeAndStatusOrderByIdAsc(OutboxEventType outboxEventType, OutboxStatus outboxStatus, ScrollPosition position);
}