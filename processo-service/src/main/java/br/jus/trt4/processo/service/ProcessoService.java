package br.jus.trt4.processo.service;

import br.jus.trt4.processo.domain.StatusProcesso;
import br.jus.trt4.processo.dto.request.ProcessoRequestDTO;
import br.jus.trt4.processo.dto.response.ProcessoResponseDTO;

import java.util.List;

/**
 * Interface separada da implementação ({@code impl.ProcessoServiceImpl}) — convenção herdada do
 * Java EE clássico (facilita mock em teste e troca de implementação). Em Spring/Mockito modernos
 * isso é OPCIONAL (dá para mockar a classe concreta direto), mas é o padrão que o mercado/edital
 * ainda espera ver — ver docs/03.
 */
public interface ProcessoService {

    ProcessoResponseDTO criar(ProcessoRequestDTO dto);

    ProcessoResponseDTO buscarPorId(Long id);

    List<ProcessoResponseDTO> listar(StatusProcesso status);

    ProcessoResponseDTO arquivar(Long id);
}
