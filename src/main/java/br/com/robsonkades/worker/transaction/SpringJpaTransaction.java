package br.com.robsonkades.worker.transaction;

import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

@Component
public class SpringJpaTransaction implements Transaction {

    private final Map<TransactionSpec, TransactionTemplate> cache = new ConcurrentHashMap<>();
    private final PlatformTransactionManager platformTransactionManager;

    public SpringJpaTransaction(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    @Override
    public <T> T execute(final Supplier<T> action) {
        TransactionTemplate template = getTemplate(new TransactionSpec());
        return template.execute(status -> action.get());
    }

    @Override
    public <T> T execute(final TransactionSpec transactionSpec, Supplier<T> action) {
        TransactionTemplate template = getTemplate(transactionSpec);
        return template.execute(status -> action.get());
    }

    @Override
    public void execute(final Runnable action) {
        TransactionTemplate template = getTemplate(new TransactionSpec());
        template.executeWithoutResult(status -> action.run());
    }

    @Override
    public void execute(final TransactionSpec transactionSpec, Runnable action) {
        TransactionTemplate template = getTemplate(transactionSpec);
        template.executeWithoutResult(status -> action.run());
    }

    private TransactionTemplate getTemplate(TransactionSpec spec) {
        return cache.computeIfAbsent(spec, transactionSpec -> {
            TransactionTemplate template = new TransactionTemplate(platformTransactionManager);
            template.setReadOnly(transactionSpec.isReadOnly());
            template.setIsolationLevel(transactionSpec.getIsolation());
            template.setPropagationBehavior(transactionSpec.getPropagation());
            template.setTimeout(transactionSpec.getTimeout());
            return template;
        });
    }
}
