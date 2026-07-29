package br.jus.trt4.processo.security;

import br.jus.trt4.processo.dto.response.ErroResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Complemento do {@link JwtAuthEntryPoint} (ver o javadoc completo lá): este trata o outro lado da
 * mesma moeda — usuário AUTENTICADO (token válido), mas sem a role exigida por um
 * {@code @PreAuthorize} (ver ProcessoController/MovimentacaoController). 401 = "eu não sei quem
 * você é"; 403 = "eu sei quem você é, mas você não pode fazer isso".
 */
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public JwtAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                        AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErroResponseDTO erro = new ErroResponseDTO(HttpStatus.FORBIDDEN.value(),
                "Acesso negado: você não tem permissão para executar esta ação.");
        objectMapper.writeValue(response.getWriter(), erro);
    }
}
