package br.jus.trt4.processo.service;

import br.jus.trt4.processo.dto.request.MovimentacaoRequestDTO;
import br.jus.trt4.processo.dto.response.MovimentacaoResponseDTO;

import java.util.List;

public interface MovimentacaoService {

    MovimentacaoResponseDTO adicionar(Long processoId, MovimentacaoRequestDTO dto);

    List<MovimentacaoResponseDTO> listarPorProcesso(Long processoId);
}
