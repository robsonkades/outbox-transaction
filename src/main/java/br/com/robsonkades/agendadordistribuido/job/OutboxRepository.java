package br.com.robsonkades.agendadordistribuido;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    @QueryHints({
            @QueryHint(
                    name = "javax.persistence.lock.timeout",
                    value = "-2"  //LockOptions.SKIP_LOCKED
            )
    })
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<OutboxEvent> findTop500ByEventTypeAndStatus(String eventType, String status);
}