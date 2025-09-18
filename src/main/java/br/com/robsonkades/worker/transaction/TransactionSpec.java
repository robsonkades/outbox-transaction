package br.com.robsonkades.worker.transaction;

import org.springframework.transaction.TransactionDefinition;

public class TransactionSpec {

    private boolean readOnly = false;
    private int isolation = TransactionDefinition.ISOLATION_DEFAULT;
    private int propagation = TransactionDefinition.PROPAGATION_REQUIRED;
    private int timeout = TransactionDefinition.TIMEOUT_DEFAULT;

    public static TransactionSpec readOnly() {
        TransactionSpec spec = new TransactionSpec();
        spec.setReadOnly(true);
        return spec;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public int getIsolation() {
        return isolation;
    }

    public void setIsolation(int isolation) {
        this.isolation = isolation;
    }

    public int getPropagation() {
        return propagation;
    }

    public void setPropagation(int propagation) {
        this.propagation = propagation;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        TransactionSpec that = (TransactionSpec) o;
        return isReadOnly() == that.isReadOnly() && getIsolation() == that.getIsolation() && getPropagation() == that.getPropagation() && getTimeout() == that.getTimeout();
    }

    @Override
    public int hashCode() {
        int result = Boolean.hashCode(isReadOnly());
        result = 31 * result + getIsolation();
        result = 31 * result + getPropagation();
        result = 31 * result + getTimeout();
        return result;
    }
}
