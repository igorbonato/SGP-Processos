package br.jus.trt4.processo.domain;

/**
 * Situação atual de um {@link Processo}. Governa a regra de negócio principal deste módulo: um
 * processo ARQUIVADO não aceita novas movimentações (ver {@link Processo#adicionarMovimentacao}).
 *
 * Paralelo .NET: um enum comum, igual C# — a diferença relevante está em COMO ele é persistido,
 * não em como é declarado (ver {@link Processo}, anotação @Enumerated).
 */
public enum StatusProcesso {
    ATIVO,
    SUSPENSO,
    ARQUIVADO
}
