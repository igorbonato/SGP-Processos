/**
 * Camada de regra de negócio: interfaces aqui, implementações em {@code impl/}. Orquestra
 * repositories, decide fronteiras de transação ({@code @Transactional}) e é o único lugar (além
 * das próprias entidades) que conhece as invariantes do domínio.
 */
package br.jus.trt4.processo.service;
