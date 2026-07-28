/**
 * Módulo de Service Discovery (Netflix Eureka) da plataforma SGP-Processos.
 *
 * Contém apenas a classe de bootstrap ({@link br.jus.trt4.eureka.EurekaServerApplication}) — não
 * há controllers, services ou entidades aqui porque este módulo não tem regra de negócio própria,
 * ele é infraestrutura de plataforma (ver docs/01-arquitetura-geral.md).
 *
 * {@code package-info.java} é a forma padrão do Java de documentar um pacote inteiro — não tem
 * paralelo direto em C# (o namespace não carrega um "resumo" próprio); o mais próximo seria um
 * comentário XML no topo de um arquivo AssemblyInfo.cs, mas aquilo documenta o assembly inteiro,
 * não um namespace específico.
 */
package br.jus.trt4.eureka;
