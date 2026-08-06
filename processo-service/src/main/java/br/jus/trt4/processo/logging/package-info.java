/**
 * Instrumentação de observabilidade (Fase 8): hoje só o {@link
 * br.jus.trt4.processo.logging.RequestIdFilter}, que popula o MDC para correlacionar logs de uma
 * mesma requisição no Kibana. A configuração de formato/destino do log em si (JSON, appender TCP
 * para o Logstash) fica em {@code src/main/resources/logback-spring.xml}, não em código Java.
 */
package br.jus.trt4.processo.logging;
