package br.jus.trt4.processo.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * ---------------------------------------------------------------------------------------------
 * OncePerRequestFilter — especialização de {@code javax.servlet.Filter} (a especificação Servlet,
 * ver docs/05) que o Spring oferece garantindo que {@code doFilterInternal} rode EXATAMENTE uma
 * vez por requisição, mesmo em cenários de forward/include internos do servlet container onde um
 * Filter "cru" poderia ser re-executado sem essa garantia.
 * ---------------------------------------------------------------------------------------------
 * Este filtro roda em TODA requisição (registrado no SecurityConfig via
 * {@code addFilterBefore}), procura um JWT no header, e se for válido, popula o
 * {@code SecurityContextHolder} — é isso que faz `@PreAuthorize` e `anyRequest().authenticated()`
 * (ver SecurityConfig) enxergarem "quem está fazendo esta requisição".
 *
 * Paralelo .NET: o equivalente mais próximo é o middleware de autenticação JWT do
 * `Microsoft.AspNetCore.Authentication.JwtBearer` registrado em `app.UseAuthentication()` — a
 * diferença é que lá o middleware vem pronto do framework; aqui escrevemos o nosso porque o
 * Spring Security não assume nenhum formato de token específico por padrão.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_AUTORIZACAO = "Authorization";
    private static final String PREFIXO_BEARER = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = extrairToken(request);

        if (token != null && jwtTokenProvider.validarToken(token)) {
            String username = jwtTokenProvider.obterUsername(token);
            List<GrantedAuthority> authorities = jwtTokenProvider.obterAuthorities(token);

            // UsernamePasswordAuthenticationToken aqui NÃO representa "usuário e senha digitados
            // agora" — é só a implementação padrão de Authentication que o Spring Security usa
            // para carregar "quem é o usuário + quais permissões ele tem" no contexto de
            // segurança. O segundo argumento (credenciais) é null de propósito: neste ponto o
            // token JÁ foi validado, não sobrou senha nenhuma para checar de novo.
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // A partir daqui, QUALQUER código mais adiante na requisição (controllers,
            // @PreAuthorize, SecurityContextHolder.getContext().getAuthentication() em qualquer
            // service) enxerga este usuário como autenticado — pelo resto desta única requisição.
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // Continua a cadeia de filtros/DispatcherServlet mesmo se NÃO havia token (ou era
        // inválido) — não barramos a requisição aqui. Quem decide se ISSO é um problema é a regra
        // de autorização do SecurityConfig (anyRequest().authenticated()): se o endpoint exige
        // autenticação e ninguém foi setado no contexto, o Spring Security barra mais adiante,
        // acionando o JwtAuthEntryPoint (401) — não este filtro.
        filterChain.doFilter(request, response);
    }

    private String extrairToken(HttpServletRequest request) {
        String header = request.getHeader(HEADER_AUTORIZACAO);
        if (StringUtils.hasText(header) && header.startsWith(PREFIXO_BEARER)) {
            return header.substring(PREFIXO_BEARER.length());
        }
        return null;
    }
}
