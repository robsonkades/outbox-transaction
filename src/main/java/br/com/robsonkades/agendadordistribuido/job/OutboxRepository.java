package br.com.robsonkades.agendadordistribuido.job;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @Lock(LockModeType.NONE)
    Window<OutboxEvent> findFirst50ByOrderByIdDesc(ScrollPosition position);

    @QueryHints({
            @QueryHint(
                    name = "jakarta.persistence.lock.timeout",
                    value = "-2"  //LockOptions.SKIP_LOCKED
            )
    })
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Window<OutboxEvent> findFirst50ByEventTypeAndStatusOrderByIdAsc(EventType eventType, Status status, ScrollPosition position);


    @Transactional(readOnly = true, isolation = Isolation.READ_UNCOMMITTED)
    Optional<OutboxEvent> findTop1ByEventTypeAndStatusOrderByIdDesc(EventType eventType, Status status);
}