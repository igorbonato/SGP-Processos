package br.jus.trt4.processo.controller;

import br.jus.trt4.processo.dto.request.MovimentacaoRequestDTO;
import br.jus.trt4.processo.dto.response.MovimentacaoResponseDTO;
import br.jus.trt4.processo.service.MovimentacaoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

// Controller próprio para o sub-recurso "movimentações" de um processo — path aninhado sob
// /api/processos/{processoId}, porque uma Movimentacao só existe vinculada a um Processo (ver
// docs/02): não faz sentido uma rota "/api/movimentacoes" solta, sem contexto de qual processo.
@RestController
@RequestMapping("/api/processos/{processoId}/movimentacoes")
public class MovimentacaoController {

    private final MovimentacaoService movimentacaoService;

    public MovimentacaoController(MovimentacaoService movimentacaoService) {
        this.movimentacaoService = movimentacaoService;
    }

    @PostMapping
    public ResponseEntity<MovimentacaoResponseDTO> adicionar(@PathVariable Long processoId,
                                                              @Valid @RequestBody MovimentacaoRequestDTO dto) {
        MovimentacaoResponseDTO criada = movimentacaoService.adicionar(processoId, dto);
        // Sem Location aqui de propósito: movimentação não tem endpoint de busca individual
        // própria (só listagem por processo, abaixo) — devolver 201 com o corpo já é suficiente.
        return ResponseEntity.status(HttpStatus.CREATED).body(criada);
    }

    @GetMapping
    public ResponseEntity<List<MovimentacaoResponseDTO>> listar(@PathVariable Long processoId) {
        return ResponseEntity.ok(movimentacaoService.listarPorProcesso(processoId));
    }
}
