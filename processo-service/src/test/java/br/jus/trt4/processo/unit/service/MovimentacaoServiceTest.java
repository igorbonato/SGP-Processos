package br.jus.trt4.processo.unit.service;

import br.jus.trt4.processo.domain.Movimentacao;
import br.jus.trt4.processo.domain.Processo;
import br.jus.trt4.processo.domain.TipoMovimentacao;
import br.jus.trt4.processo.dto.request.MovimentacaoRequestDTO;
import br.jus.trt4.processo.dto.response.MovimentacaoResponseDTO;
import br.jus.trt4.processo.exception.ProcessoNaoEncontradoException;
import br.jus.trt4.processo.exception.RegraDeNegocioException;
import br.jus.trt4.processo.mapper.MovimentacaoMapper;
import br.jus.trt4.processo.repository.MovimentacaoRepository;
import br.jus.trt4.processo.repository.ProcessoRepository;
import br.jus.trt4.processo.service.impl.MovimentacaoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {

    @Mock
    private ProcessoRepository processoRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private MovimentacaoMapper movimentacaoMapper;

    private MovimentacaoServiceImpl movimentacaoService;

    @BeforeEach
    void setUp() {
        movimentacaoService = new MovimentacaoServiceImpl(processoRepository, movimentacaoRepository, movimentacaoMapper);
    }

    // ---------------------------------------------------------------------------------------
    // Este teste é a REGRESSÃO do bug real encontrado testando a Fase 3 (ver docs/09 e o
    // comentário em MovimentacaoServiceImpl): mapear a variável local pré-save em vez do retorno
    // de save() devolvia sempre id=null. Simulamos aqui exatamente o comportamento do
    // Hibernate real que expôs o bug — save()/merge() devolvendo uma instância DIFERENTE da que
    // foi passada, com a movimentação "final" (a que o mapper deveria usar) só disponível
    // dentro do OBJETO RETORNADO, não no que criamos no teste.
    // ---------------------------------------------------------------------------------------
    @Test
    void deveMapearAPartirDoRetornoDeSaveNaoDaVariavelOriginal() {
        Processo processoCarregado = new Processo("0001", "C", "V", LocalDate.now());
        MovimentacaoRequestDTO dto = new MovimentacaoRequestDTO();
        dto.setDescricao("Despacho");
        dto.setTipoMovimentacao(TipoMovimentacao.DESPACHO);

        // Processo "salvo" é uma instância DIFERENTE (simula o merge() do Hibernate), com uma
        // Movimentacao já "gerada" (com id, diferente da que o service criou "crua").
        Processo processoRetornadoPeloSave = new Processo("0001", "C", "V", LocalDate.now());
        Movimentacao movimentacaoComIdSimulado =
                new Movimentacao("Despacho", TipoMovimentacao.DESPACHO, LocalDateTime.now());
        processoRetornadoPeloSave.adicionarMovimentacao(movimentacaoComIdSimulado);

        MovimentacaoResponseDTO respostaEsperada = new MovimentacaoResponseDTO();

        when(processoRepository.findById(1L)).thenReturn(Optional.of(processoCarregado));
        when(processoRepository.save(any(Processo.class))).thenReturn(processoRetornadoPeloSave);
        when(movimentacaoMapper.toResponseDTO(movimentacaoComIdSimulado)).thenReturn(respostaEsperada);

        MovimentacaoResponseDTO resultado = movimentacaoService.adicionar(1L, dto);

        assertThat(resultado).isSameAs(respostaEsperada);
        // O ponto crucial: o mapper precisa ter sido chamado com a Movimentacao do processo
        // RETORNADO por save(), não com nenhuma instância criada antes dele. Se o código
        // regredisse para o bug antigo, este verify falharia (o mapper teria sido chamado com
        // outra instância, e o "when" acima nunca teria batido — o teste falharia com
        // UnnecessaryStubbingException do modo estrito do Mockito).
        verify(movimentacaoMapper).toResponseDTO(movimentacaoComIdSimulado);
    }

    @Test
    void deveLancarExcecaoQuandoProcessoNaoExiste() {
        when(processoRepository.findById(99L)).thenReturn(Optional.empty());
        MovimentacaoRequestDTO dto = new MovimentacaoRequestDTO();
        dto.setDescricao("X");
        dto.setTipoMovimentacao(TipoMovimentacao.DESPACHO);

        assertThrows(ProcessoNaoEncontradoException.class, () -> movimentacaoService.adicionar(99L, dto));

        verify(processoRepository, never()).save(any());
    }

    @Test
    void deveLancarRegraDeNegocioQuandoProcessoEstaArquivado() {
        Processo processoArquivado = new Processo("0002", "C", "V", LocalDate.now());
        processoArquivado.arquivar();

        when(processoRepository.findById(1L)).thenReturn(Optional.of(processoArquivado));

        MovimentacaoRequestDTO dto = new MovimentacaoRequestDTO();
        dto.setDescricao("Tentativa tardia");
        dto.setTipoMovimentacao(TipoMovimentacao.DESPACHO);

        assertThrows(RegraDeNegocioException.class, () -> movimentacaoService.adicionar(1L, dto));

        // A regra é da ENTIDADE (Processo.adicionarMovimentacao) — o service nem chega perto de
        // save(), porque a exceção já estourou antes.
        verify(processoRepository, never()).save(any());
    }

    @Test
    void deveListarMovimentacoesDoProcessoExistente() {
        when(processoRepository.existsById(1L)).thenReturn(true);
        List<Movimentacao> movimentacoes = Collections.emptyList();
        when(movimentacaoRepository.findByProcessoIdOrderByDataMovimentacaoDesc(1L)).thenReturn(movimentacoes);
        when(movimentacaoMapper.toResponseDTOList(movimentacoes)).thenReturn(Collections.emptyList());

        movimentacaoService.listarPorProcesso(1L);

        verify(movimentacaoRepository).findByProcessoIdOrderByDataMovimentacaoDesc(1L);
    }

    @Test
    void deveLancarExcecaoAoListarMovimentacoesDeProcessoInexistente() {
        when(processoRepository.existsById(99L)).thenReturn(false);

        assertThrows(ProcessoNaoEncontradoException.class, () -> movimentacaoService.listarPorProcesso(99L));

        verify(movimentacaoRepository, never())
                .findByProcessoIdOrderByDataMovimentacaoDesc(any());
    }
}
