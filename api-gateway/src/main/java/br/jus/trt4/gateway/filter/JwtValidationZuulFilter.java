package br.jus.trt4.gateway.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;
import java.util.List;

/**
 * ---------------------------------------------------------------------------------------------
 * ZuulFilter — o "Filter" (Servlet spec) do mundo Zuul, mas com um modelo próprio, mais rico:
 * em vez de só "antes/depois" como um javax.servlet.Filter comum, cada ZuulFilter declara um
 * {@link #filterType()} (aqui, "pre" — roda ANTES do roteamento para o serviço de destino;
 * existem também "route", "post" e "error") e uma {@link #filterOrder()} (posição relativa a
 * outros filtros do mesmo tipo).
 * ---------------------------------------------------------------------------------------------
 * Este é o "primeiro degrau" de validação de JWT descrito em docs/06: aqui só confere se o token
 * tem assinatura válida e não expirou — quem checa QUAL usuário é e QUAIS roles ele tem
 * (`@PreAuthorize`) é o processo-service, depois de a requisição já ter passado por aqui.
 * Requisição com token ruim nem chega a "acordar" o processo-service.
 *
 * @Component — precisa ser um bean Spring (diferente do {@code JwtAuthenticationFilter} do
 * processo-service, que é um {@code javax.servlet.Filter} comum, registrado manualmente no
 * SecurityConfig): o Zuul descobre e registra automaticamente qualquer bean {@code ZuulFilter}
 * presente no contexto Spring — é assim, e não por registro manual, que este filtro passa a
 * rodar em toda requisição.
 */
@Component
public class JwtValidationZuulFilter extends ZuulFilter {

    // Rotas que NÃO exigem token — login (ninguém tem token ainda) e documentação/observabilidade,
    // que devem ser acessíveis publicamente mesmo passando pelo gateway.
    private static final List<String> PATHS_PUBLICOS = Arrays.asList(
            "/auth/login", "/v3/api-docs", "/swagger-ui", "/actuator");

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        // Valor baixo = roda cedo. Precisa ser cedo o bastante para barrar a requisição ANTES do
        // filtro de roteamento de fato chamar o processo-service — mas não precisamos disputar
        // posição com os filtros internos de descoberta de rota do próprio Zuul, então um valor
        // pequeno e arbitrário (1) já resolve.
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        String path = RequestContext.getCurrentContext().getRequest().getRequestURI();
        return PATHS_PUBLICOS.stream().noneMatch(path::startsWith);
    }

    @Override
    public Object run() {
        RequestContext contexto = RequestContext.getCurrentContext();
        HttpServletRequest request = contexto.getRequest();

        String token = extrairToken(request);

        if (token == null || !tokenValido(token)) {
            // -----------------------------------------------------------------------------
            // setSendZuulResponse(false) — o coração deste filtro: diz ao Zuul "NÃO encaminhe
            // esta requisição para o serviço de destino". Sem esta chamada, mesmo respondendo
            // um status diferente aqui, o Zuul rotearia normalmente para o processo-service
            // de qualquer jeito — a requisição só é efetivamente barrada quando desligamos o
            // envio adiante explicitamente.
            // -----------------------------------------------------------------------------
            contexto.setSendZuulResponse(false);
            contexto.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            // setCharacterEncoding ANTES de escrever qualquer coisa na resposta — sem isso, o
            // acento de "inválido" chegava corrompido ("inv�lido") ao cliente: descobri isso
            // testando de verdade, o container assume um encoding padrão diferente de UTF-8
            // quando ninguém declara explicitamente.
            contexto.getResponse().setCharacterEncoding(StandardCharsets.UTF_8.name());
            contexto.getResponse().setContentType(MediaType.APPLICATION_JSON_VALUE);
            contexto.setResponseBody(
                    "{\"status\":401,\"mensagem\":\"Token JWT ausente ou inválido (validado no gateway).\"}");
        }

        // O valor de retorno de run() é ignorado pelo Zuul (existe só por causa da assinatura
        // legada da interface) — o efeito real acontece através das chamadas no RequestContext.
        return null;
    }

    private String extrairToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private boolean tokenValido(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
