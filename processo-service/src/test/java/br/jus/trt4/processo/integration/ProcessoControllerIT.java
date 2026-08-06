package br.jus.trt4.processo.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// -------------------------------------------------------------------------------------------
// @SpringBootTest — sobe o ApplicationContext Spring INTEIRO (todos os @Component/@Service/
// @Repository/@Configuration reais, banco H2 real via Flyway, MapStruct real gerado em
// compile-time, Spring Security real) — bem diferente dos testes em unit/, que não tocam em
// nada disso. É por isso que este teste é lento (segundos, não milissegundos) e por isso mora
// em integration/, rodando só via "mvn verify" (Failsafe), não em todo "mvn test" (ver
// docs/07-testes-e-qualidade.md).
//
// @AutoConfigureMockMvc — registra um MockMvc: simula requisições HTTP completas (passando pelo
// DispatcherServlet, filtros de Spring Security, @Valid, controllers) SEM abrir uma porta TCP de
// verdade — mais rápido que um HTTP client real, e sem a fragilidade de portas ocupadas.
// Paralelo .NET: o mesmo papel do `WebApplicationFactory<TEntryPoint>` + `HttpClient` de teste do
// ASP.NET Core — a diferença é que o MockMvc nem abre socket nenhum, o WebApplicationFactory abre
// um servidor de teste em memória (TestServer) que ainda simula a pilha HTTP completa.
//
// @Transactional (aqui, na classe de teste — NÃO confundir com o @Transactional dos services)
// — o Spring Test envolve CADA método de teste em uma transação própria e dá ROLLBACK
// automaticamente ao final, mesmo que o teste tenha "commitado" (chamado save()) várias vezes no
// meio do caminho. É o que permite os métodos abaixo reutilizarem números de processo entre
// execuções repetidas sem violar a constraint UNIQUE — cada teste começa e termina com o banco
// exatamente como estava antes.
// -------------------------------------------------------------------------------------------
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ProcessoControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveRecusarRequisicaoSemToken() throws Exception {
        mockMvc.perform(get("/api/processos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveCriarProcessoEDevolverPayloadMapeadoCorretamente() throws Exception {
        String token = obterToken("analista", "senha123");

        String payload = "{"
                + "\"numero\":\"IT-0001\","
                + "\"classeJudicial\":\"Reclamação Trabalhista\","
                + "\"vara\":\"1ª Vara\","
                + "\"dataAutuacao\":\"2026-01-15\","
                + "\"partes\":[{\"nome\":\"Igor Bonato\",\"tipoParte\":\"AUTOR\",\"documento\":\"111\"}]"
                + "}";

        // Esta chamada valida, numa tacada só, a cadeia inteira: @Valid (Bean Validation) ->
        // ProcessoController -> ProcessoServiceImpl -> Processo (construtor + adicionarParte) ->
        // ProcessoRepository (JPA/Hibernate real, contra H2) -> ProcessoMapper (MapStruct GERADO
        // de verdade, não mockado) -> serialização JSON (Jackson) da resposta.
        mockMvc.perform(post("/api/processos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.numero").value("IT-0001"))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.classeJudicial").value("Reclamação Trabalhista"))
                .andExpect(jsonPath("$.partes[0].nome").value("Igor Bonato"));
    }

    @Test
    void deveRetornar400QuandoProcessoNaoTemPartes() throws Exception {
        String token = obterToken("analista", "senha123");
        String payload = "{\"numero\":\"IT-0002\",\"dataAutuacao\":\"2026-01-01\",\"partes\":[]}";

        mockMvc.perform(post("/api/processos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRetornar403QuandoConsultaTentaCriarProcesso() throws Exception {
        String token = obterToken("consulta", "senha123");
        String payload = "{\"numero\":\"IT-0003\",\"dataAutuacao\":\"2026-01-01\","
                + "\"partes\":[{\"nome\":\"X\",\"tipoParte\":\"AUTOR\",\"documento\":\"1\"}]}";

        mockMvc.perform(post("/api/processos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar404ParaProcessoInexistente() throws Exception {
        String token = obterToken("analista", "senha123");

        mockMvc.perform(get("/api/processos/{id}", 999999L).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveArquivarERecusarNovaMovimentacaoDepois() throws Exception {
        String token = obterToken("analista", "senha123");

        // cria
        String payload = "{\"numero\":\"IT-0004\",\"dataAutuacao\":\"2026-01-01\","
                + "\"partes\":[{\"nome\":\"X\",\"tipoParte\":\"AUTOR\",\"documento\":\"1\"}]}";
        MvcResult resultadoCriacao = mockMvc.perform(post("/api/processos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(resultadoCriacao.getResponse().getContentAsString());
        long id = json.get("id").asLong();

        // arquiva
        mockMvc.perform(patch("/api/processos/{id}/arquivar", id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARQUIVADO"));

        // tenta adicionar movimentação -> regra de negócio (RegraDeNegocioException -> 422)
        String movimentacaoPayload = "{\"descricao\":\"Tentativa tardia\",\"tipoMovimentacao\":\"DESPACHO\"}";
        mockMvc.perform(post("/api/processos/{id}/movimentacoes", id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movimentacaoPayload))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.mensagem").exists());
    }

    /** Login real via HTTP (não @WithMockUser) — exercita AuthController + SecurityConfig +
     *  JwtTokenProvider de verdade, não só os endpoints de domínio. */
    private String obterToken(String username, String password) throws Exception {
        String payload = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

        MvcResult resultado = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(resultado.getResponse().getContentAsString());
        String token = json.get("token").asText();
        assertThat(token).isNotBlank();
        return token;
    }
}
