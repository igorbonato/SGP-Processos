package br.jus.trt4.processo.service.impl;

import br.jus.trt4.processo.domain.Movimentacao;
import br.jus.trt4.processo.domain.Processo;
import br.jus.trt4.processo.dto.request.MovimentacaoRequestDTO;
import br.jus.trt4.processo.dto.response.MovimentacaoResponseDTO;
import br.jus.trt4.processo.exception.ProcessoNaoEncontradoException;
import br.jus.trt4.processo.mapper.MovimentacaoMapper;
import br.jus.trt4.processo.repository.MovimentacaoRepository;
import br.jus.trt4.processo.repository.ProcessoRepository;
import br.jus.trt4.processo.service.MovimentacaoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MovimentacaoServiceImpl implements MovimentacaoService {

    private final ProcessoRepository processoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final MovimentacaoMapper movimentacaoMapper;

    public MovimentacaoServiceImpl(ProcessoRepository processoRepository,
                                    MovimentacaoRepository movimentacaoRepository,
                                    MovimentacaoMapper movimentacaoMapper) {
        this.processoRepository = processoRepository;
        this.movimentacaoRepository = movimentacaoRepository;
        this.movimentacaoMapper = movimentacaoMapper;
    }

    @Override
    @Transactional
    public MovimentacaoResponseDTO adicionar(Long processoId, MovimentacaoRequestDTO dto) {
        Processo processo = processoRepository.findById(processoId)
                .orElseThrow(() -> new ProcessoNaoEncontradoException(processoId));

        // O timestamp é decidido AQUI (momento em que a movimentação é efetivamente registrada),
        // não recebido do cliente — ver comentário em MovimentacaoRequestDTO.
        Movimentacao movimentacao = new Movimentacao(dto.getDescricao(), dto.getTipoMovimentacao(),
                LocalDateTime.now());

        // Pode lançar RegraDeNegocioException se o processo estiver ARQUIVADO — a exceção sobe
        // sem tratamento aqui de propósito (é unchecked, ver RegraDeNegocioException.java) e será
        // capturada pelo GlobalExceptionHandler, virando um HTTP 422. Como a exceção é lançada
        // ANTES de qualquer persistência, a transação sequer chega a commitar nada.
        processo.adicionarMovimentacao(movimentacao);

        // -------------------------------------------------------------------------------------
        // BUG REAL encontrado testando este endpoint na prática (ver docs/09): "processo" já tem
        // id (foi carregado via findById), então processoRepository.save(...) executa um
        // entityManager.merge(...) por baixo dos panos — NÃO um persist(). A própria especificação
        // JPA é explícita: merge() pode devolver uma instância DIFERENTE da que foi passada, e é
        // essa instância retornada (não a original) que fica com os campos gerados (como o "id"
        // desta nova Movimentacao) populados. Guardar o retorno de save() e buscar a movimentação
        // recém-persistida NELE é obrigatório — mapear a variável "movimentacao" original (como a
        // primeira versão deste método fazia) devolvia sempre id=null no corpo da resposta, mesmo
        // com a linha já gravada corretamente no banco.
        // -------------------------------------------------------------------------------------
        Processo processoSalvo = processoRepository.save(processo);
        List<Movimentacao> movimentacoes = processoSalvo.getMovimentacoes();
        Movimentacao movimentacaoSalva = movimentacoes.get(movimentacoes.size() - 1);

        return movimentacaoMapper.toResponseDTO(movimentacaoSalva);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimentacaoResponseDTO> listarPorProcesso(Long processoId) {
        if (!processoRepository.existsById(processoId)) {
            throw new ProcessoNaoEncontradoException(processoId);
        }
        List<Movimentacao> movimentacoes =
                movimentacaoRepository.findByProcessoIdOrderByDataMovimentacaoDesc(processoId);
        return movimentacaoMapper.toResponseDTOList(movimentacoes);
    }
}
