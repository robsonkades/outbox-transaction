package br.com.robsonkades.worker.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Objects;

@Entity(name = "Outbox")
@Table(name = "outbox_event")
public class OutboxJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JdbcTypeCode(SqlTypes.BIGINT)
    private Long id;

    @Column(name = "aggregate_id", nullable = false)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private String aggregateId;

    @Column(name = "aggregate_type", nullable = false, length = 50)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private String aggregateType;

    @Column(name = "event_type", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @JdbcTypeCode(SqlTypes.TINYINT)
    private OutboxEventType eventType;

    @Column(nullable = false, length = 2000, columnDefinition = "NVARCHAR(2000)")
    @JdbcTypeCode(SqlTypes.VARCHAR)
    private String payload;

    @Column(name = "status", nullable = false)
    @Enumerated
    @JdbcTypeCode(SqlTypes.TINYINT)
    private OutboxStatus status;

    @Column(name = "created_at", nullable = false)
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant createdAt;

    @Column(name = "processed_at")
    @JdbcTypeCode(SqlTypes.TIMESTAMP)
    private Instant processedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void setAggregateId(String aggregateId) {
        this.aggregateId = aggregateId;
    }

    public String getAggregateType() {
        return aggregateType;
    }

    public void setAggregateType(String aggregateType) {
        this.aggregateType = aggregateType;
    }

    public OutboxEventType getEventType() {
        return eventType;
    }

    public void setEventType(OutboxEventType outboxEventType) {
        this.eventType = outboxEventType;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public void setStatus(OutboxStatus outboxStatus) {
        this.status = outboxStatus;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(Instant processedAt) {
        this.processedAt = processedAt;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        OutboxJpaEntity that = (OutboxJpaEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" +
                "id = " + id + ", " +
                "aggregateId = " + aggregateId + ", " +
                "aggregateType = " + aggregateType + ", " +
                "eventType = " + eventType + ", " +
                "payload = " + payload + ", " +
                "status = " + status + ", " +
                "createdAt = " + createdAt + ", " +
                "processedAt = " + processedAt + ")";
    }
}
