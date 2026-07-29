/**
 * Camada de apresentação (REST): expõe endpoints HTTP, valida formato de entrada ({@code @Valid})
 * e traduz o retorno do service em {@code ResponseEntity} com o status HTTP correto. Não contém
 * regra de negócio — isso vive em {@code service/} e nas entidades de {@code domain/}.
 */
package br.jus.trt4.processo.controller;
