package br.jus.trt4.processo.exception;

/**
 * Lançada quando uma operação viola uma regra de negócio do domínio (ex.: adicionar movimentação
 * em processo arquivado). Ainda não temos um {@code @ControllerAdvice} tratando-a — isso chega
 * na Fase 3, junto com os controllers REST que vão precisar traduzir esta exceção em HTTP 422/400.
 *
 * ---------------------------------------------------------------------------------------------
 * Por que estender RuntimeException (unchecked) e não Exception (checked)?
 * ---------------------------------------------------------------------------------------------
 * Isto é uma decisão de design que EXISTE em Java e NÃO EXISTE em C#: o C# não tem o conceito de
 * "checked exception" — toda exceção em C# se comporta como unchecked em Java (você nunca é
 * obrigado pelo compilador a declarar ou capturar). Em Java, uma exceção que estende
 * {@code Exception} diretamente (checked) OBRIGA quem chama o método a capturá-la ou redeclarar
 * com {@code throws} — o compilador barra a compilação se você ignorar. Uma exceção que estende
 * {@code RuntimeException} (unchecked) não tem essa obrigação, igual toda exceção em C#.
 *
 * Escolhemos unchecked de propósito: regra de negócio violada é um erro que queremos propagar
 * até uma camada central de tratamento (o futuro {@code GlobalExceptionHandler}), sem poluir toda
 * a cadeia de chamadas no meio do caminho (services, controllers) com {@code throws
 * RegraDeNegocioException} ou blocos {@code try/catch} redundantes — exatamente como você já
 * naturalmente faz em C#, onde essa escolha nem existe.
 *
 * Ver docs/09-notas-para-prova.md para o motivo disso ser pergunta clássica de entrevista/prova.
 */
public class RegraDeNegocioException extends RuntimeException {

    public RegraDeNegocioException(String message) {
        super(message);
    }
}
