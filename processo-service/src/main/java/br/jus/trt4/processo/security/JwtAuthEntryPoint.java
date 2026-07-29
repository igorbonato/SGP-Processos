package br.jus.trt4.processo.security;

import br.jus.trt4.processo.dto.response.ErroResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * ---------------------------------------------------------------------------------------------
 * Por que isto NÃO é um método do GlobalExceptionHandler (@ExceptionHandler)?
 * ---------------------------------------------------------------------------------------------
 * {@code @RestControllerAdvice}/{@code @ExceptionHandler} (ver GlobalExceptionHandler) só
 * intercepta exceções lançadas DENTRO da invocação normal de um controller pelo Spring MVC. Toda
 * {@link AuthenticationException} (token ausente/inválido, credenciais erradas no login) —
 * mesmo quando lançada de dentro de um método de controller, como o login em AuthController — é
 * capturada ANTES disso, pelo {@code ExceptionTranslationFilter} do Spring Security, que entra em
 * ação bem mais cedo na cadeia de filtros e nunca deixa a exceção chegar ao mecanismo de
 * {@code @ExceptionHandler}. Por isso esse tipo de falha tem seu PRÓPRIO mecanismo de
 * configuração: um {@link AuthenticationEntryPoint} (aqui) para 401, e um
 * {@code AccessDeniedHandler} (ver JwtAccessDeniedHandler) para 403 — registrados no
 * SecurityConfig via {@code .exceptionHandling()}, não como {@code @ExceptionHandler}.
 *
 * Paralelo .NET: comparável à diferença entre um middleware de exceção genérico
 * (`app.UseExceptionHandler`) e o tratamento de `401`/`403` que o middleware de autenticação do
 * ASP.NET Core já intercepta sozinho antes de qualquer filtro de exceção de nível de aplicação.
 * ---------------------------------------------------------------------------------------------
 */
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public JwtAuthEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                          AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        // Mensagem deliberadamente genérica: este MESMO entry point trata tanto "token
        // ausente/inválido em endpoint protegido" quanto "usuário/senha errados no /auth/login"
        // (ambos são AuthenticationException — ver javadoc da classe) — confirmado testando os
        // dois cenários de verdade. Uma mensagem específica demais ("token expirado") ficaria
        // errada para o caso de login, e vice-versa.
        ErroResponseDTO erro = new ErroResponseDTO(HttpStatus.UNAUTHORIZED.value(),
                "Falha de autenticação: credenciais ou token inválidos.");
        objectMapper.writeValue(response.getWriter(), erro);
    }
}
