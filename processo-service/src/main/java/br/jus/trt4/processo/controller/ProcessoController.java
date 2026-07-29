package br.jus.trt4.processo.controller;

import br.jus.trt4.processo.domain.StatusProcesso;
import br.jus.trt4.processo.dto.request.ProcessoRequestDTO;
import br.jus.trt4.processo.dto.response.ProcessoResponseDTO;
import br.jus.trt4.processo.service.ProcessoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.List;

// -------------------------------------------------------------------------------------------
// @RestController = @Controller + @ResponseBody. @Controller sozinho faria o Spring MVC tratar o
// retorno dos métodos como o NOME DE UMA VIEW (server-side rendering, ex.: JSP/Thymeleaf);
// @ResponseBody diz "não, serialize o valor de retorno direto no corpo da resposta (JSON, via
// Jackson)". @RestController existe só para não precisarmos repetir @ResponseBody em cada método.
// Paralelo .NET: [ApiController] no ASP.NET Core — mesma ideia de "isto é uma API, não uma view".
//
// @RequestMapping("/api/processos") — prefixo de rota comum a todos os métodos da classe.
// Paralelo .NET: [Route("api/processos")] no topo do controller.
// -------------------------------------------------------------------------------------------
@RestController
@RequestMapping("/api/processos")
public class ProcessoController {

    private final ProcessoService processoService;

    public ProcessoController(ProcessoService processoService) {
        this.processoService = processoService;
    }

    @PostMapping
    // -----------------------------------------------------------------------------------------
    // @Valid — dispara o Bean Validation nas anotações do ProcessoRequestDTO (@NotBlank,
    // @NotEmpty, o @Valid aninhado nas Partes) ANTES do corpo do método rodar. Se alguma
    // validação falhar, o Spring lança MethodArgumentNotValidException — nunca chega a entrar
    // aqui; quem trata isso é o GlobalExceptionHandler, devolvendo HTTP 400.
    // @RequestBody — desserializa o corpo JSON da requisição para um ProcessoRequestDTO (via
    // Jackson). Paralelo .NET: [FromBody] combinado ao model binding automático com
    // ModelState.IsValid (aqui, a validação falha já vira exceção, não fica só marcada num
    // "ModelState" para você checar manualmente).
    // -----------------------------------------------------------------------------------------
    public ResponseEntity<ProcessoResponseDTO> criar(@Valid @RequestBody ProcessoRequestDTO dto) {
        ProcessoResponseDTO criado = processoService.criar(dto);

        // Convenção REST: uma criação (201 Created) deve devolver o header "Location" apontando
        // para a URL do recurso recém-criado. ServletUriComponentsBuilder monta essa URL a partir
        // da requisição atual ("/api/processos" + "/{id}"), sem hardcodar host/porta.
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(criado.getId())
                .toUri();

        return ResponseEntity.created(location).body(criado);
    }

    @GetMapping("/{id}")
    // @PathVariable extrai o "{id}" da URL. Paralelo .NET: [FromRoute] (ou implícito, se o nome
    // do parâmetro bate com o nome do segmento de rota).
    public ResponseEntity<ProcessoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(processoService.buscarPorId(id));
    }

    @GetMapping
    // @RequestParam(required = false) — filtro OPCIONAL via query string:
    // GET /api/processos?status=ARQUIVADO. Sem o parâmetro, "status" chega null e o service lista
    // tudo. Paralelo .NET: [FromQuery] com um parâmetro nullable/opcional na action.
    public ResponseEntity<List<ProcessoResponseDTO>> listar(
            @RequestParam(required = false) StatusProcesso status) {
        return ResponseEntity.ok(processoService.listar(status));
    }

    @PatchMapping("/{id}/arquivar")
    // PATCH (não PUT/POST) por semântica REST: esta operação altera PARCIALMENTE o recurso (só o
    // status), não o substitui inteiro (seria PUT) nem cria um novo (seria POST).
    public ResponseEntity<ProcessoResponseDTO> arquivar(@PathVariable Long id) {
        return ResponseEntity.ok(processoService.arquivar(id));
    }
}
