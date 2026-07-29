/**
 * Tudo relacionado a JWT: emissão/validação do token ({@link
 * br.jus.trt4.processo.security.JwtTokenProvider}), o filtro que intercepta toda requisição
 * ({@link br.jus.trt4.processo.security.JwtAuthenticationFilter}), e os dois pontos de resposta
 * para falhas de autenticação/autorização ({@link br.jus.trt4.processo.security.JwtAuthEntryPoint},
 * {@link br.jus.trt4.processo.security.JwtAccessDeniedHandler}) — ver o porquê desses dois não
 * serem {@code @ExceptionHandler} no javadoc de {@code JwtAuthEntryPoint}.
 */
package br.jus.trt4.processo.security;
