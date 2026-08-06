package br.jus.trt4.processo.logging;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * ---------------------------------------------------------------------------------------------
 * MDC (Mapped Diagnostic Context) — o mecanismo do SLF4J para anexar um valor de contexto (aqui,
 * "requestId") a TODA linha de log emitida dentro da mesma requisição/thread, sem precisar passar
 * esse valor manualmente para cada chamada de log espalhada pelo código. O `logback-spring.xml`
 * referencia esse valor via `%X{requestId}` (console) e o LogstashEncoder o inclui automaticamente
 * como um CAMPO JSON pesquisável no Elasticsearch — é assim que, no Kibana, dá para filtrar TODAS
 * as linhas de log de uma única requisição específica, mesmo que ela tenha passado por várias
 * classes diferentes.
 *
 * Paralelo .NET: o mesmo papel de `ILogger.BeginScope(...)` (built-in) ou dos "enrichers" do
 * Serilog (`LogContext.PushProperty(...)`) — a diferença é que o SLF4J/Logback usa um
 * `ThreadLocal` simples por baixo dos panos (por isso o `MDC.remove` no `finally` é OBRIGATÓRIO,
 * ver comentário abaixo), enquanto o `BeginScope` do .NET já devolve um `IDisposable` que limpa
 * sozinho ao sair do `using`.
 * ---------------------------------------------------------------------------------------------
 */
// @Order(Ordered.HIGHEST_PRECEDENCE) — garante que este filtro rode ANTES até do filtro de
// segurança (JwtAuthenticationFilter, registrado via addFilterBefore no SecurityConfig) — assim,
// mesmo uma requisição barrada por falta de token (401, antes de chegar em qualquer controller)
// ainda tem um requestId nos logs, útil para correlacionar um 401 no Kibana com o que o cliente
// realmente enviou.
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class RequestIdFilter extends OncePerRequestFilter {

    private static final String MDC_KEY = "requestId";
    private static final String RESPONSE_HEADER = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String requestId = UUID.randomUUID().toString();
        MDC.put(MDC_KEY, requestId);
        // Devolver o id no header da resposta também ajuda quem está do lado de fora (um cliente,
        // ou o api-gateway) a citar esse id ao reportar um problema, sem precisar abrir o Kibana.
        response.setHeader(RESPONSE_HEADER, requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // -----------------------------------------------------------------------------
            // MDC.remove no finally — NÃO é só boa prática, é obrigatório. O Tomcat REUTILIZA
            // threads de um pool entre requisições diferentes; sem limpar, o valor de
            // "requestId" da requisição A vazaria e apareceria (errado) nos logs da próxima
            // requisição B que por acaso caísse na MESMA thread reciclada.
            // -----------------------------------------------------------------------------
            MDC.remove(MDC_KEY);
        }
    }
}
