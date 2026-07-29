/**
 * Módulo API Gateway (Zuul) da plataforma SGP-Processos — porta de entrada única, roteando via
 * Eureka para o processo-service. Contém a classe de bootstrap
 * ({@link br.jus.trt4.gateway.ApiGatewayApplication}) e o filtro de validação de JWT em
 * {@link br.jus.trt4.gateway.filter}. Sem controllers/services/entidades próprios — este módulo
 * é infraestrutura de plataforma, sem regra de negócio (ver docs/01-arquitetura-geral.md).
 */
package br.jus.trt4.gateway;
