package br.com.robsonkades.worker.transaction;

import java.util.function.Supplier;

/**
 * {@code UnitOfWork} define uma abstração para execução de operações dentro de um contexto transacional.
 *
 * <p>É inspirada no padrão <b>Unit of Work</b>, que garante que todas as operações executadas durante uma
 * unidade lógica de trabalho sejam feitas de forma atômica, com commit no final ou rollback em caso de falha.</p>
 *
 * <p>Essa interface desacopla a lógica de transação da aplicação, permitindo que diferentes implementações
 * (como baseadas em {@code TransactionTemplate} ou {@code @Transactional}) sejam utilizadas sem alterar
 * os serviços de domínio ou aplicação.</p>
 *
 * <p>Também permite customizar aspectos transacionais como isolamento, propagação, timeout e read-only,
 * através do {@link TransactionSpec}.</p>
 *
 * <p>Exemplo de uso básico:
 * <pre>{@code
 * Pedido pedido = unitOfWork.execute(() -> pedidoRepository.save(pedido));
 * }</pre>
 *
 * <p>Exemplo com configuração personalizada:
 * <pre>{@code
 * TransactionSpec spec = TransactionSpec.readOnly();
 * unitOfWork.execute(spec, () -> pedidoRepository.findAll());
 * }</pre>
 *
 * @see TransactionSpec
 */
public interface Transaction {

    /**
     * Executa uma operação de leitura ou escrita dentro de uma transação com configuração padrão.
     *
     * <p>Geralmente usa:
     * <ul>
     *   <li>Propagação: {@code REQUIRED}</li>
     *   <li>Isolamento: {@code DEFAULT}</li>
     *   <li>ReadOnly: {@code false}</li>
     * </ul>
     *
     * @param action operação a ser executada
     * @param <T> tipo de retorno
     * @return resultado da operação
     * @throws RuntimeException se a execução falhar
     */
    <T> T execute(Supplier<T> action);

    /**
     * Executa uma operação de leitura ou escrita dentro de uma transação com as configurações fornecidas.
     *
     * <p>Permite definir comportamento como {@code readOnly}, nível de isolamento,
     * propagação e timeout via {@link TransactionSpec}.
     *
     * @param spec especificação da transação
     * @param action operação a ser executada
     * @param <T> tipo de retorno
     * @return resultado da operação
     * @throws RuntimeException se a execução falhar
     */
    <T> T execute(TransactionSpec spec, Supplier<T> action);

    /**
     * Executa uma operação sem retorno dentro de uma transação com configuração padrão.
     *
     * <p>Geralmente usa:
     * <ul>
     *   <li>Propagação: {@code REQUIRED}</li>
     *   <li>Isolamento: {@code DEFAULT}</li>
     *   <li>ReadOnly: {@code false}</li>
     * </ul>
     *
     * @param action operação a ser executada
     * @throws RuntimeException se a execução falhar
     */
    void execute(Runnable action);

    /**
     * Executa uma operação sem retorno dentro de uma transação com as configurações fornecidas.
     *
     * <p>Permite definir comportamento como {@code readOnly}, nível de isolamento,
     * propagação e timeout via {@link TransactionSpec}.
     *
     * @param spec especificação da transação
     * @param action operação a ser executada
     * @throws RuntimeException se a execução falhar
     */
    void execute(TransactionSpec spec, Runnable action);
}
