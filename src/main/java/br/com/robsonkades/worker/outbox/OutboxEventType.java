package br.com.robsonkades.worker.outbox;

public enum OutboxEventType {
    ORDER_CREATED,
    ORDER_SHIPPED,
    PAYMENT_CONFIRMED,
    INVOICE_GENERATED
}
