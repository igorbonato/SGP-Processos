package br.jus.trt4.processo.unit.service;

import br.jus.trt4.processo.domain.Processo;
import br.jus.trt4.processo.domain.StatusProcesso;
import br.jus.trt4.processo.dto.request.ParteRequestDTO;
import br.jus.trt4.processo.dto.request.ProcessoRequestDTO;
import br.jus.trt4.processo.dto.response.ProcessoResponseDTO;
import br.jus.trt4.processo.domain.TipoParte;
import br.jus.trt4.processo.exception.ProcessoNaoEncontradoException;
import br.jus.trt4.processo.mapper.ProcessoMapper;
import br.jus.trt4.processo.repository.ProcessoRepository;
import br.jus.trt4.processo.service.impl.ProcessoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// -------------------------------------------------------------------------------------------
// @ExtendWith(MockitoExtension.class) — o "JUnit 5 way" de ligar o Mockito a este teste: é ela
// quem processa as anotações @Mock abaixo (instanciando os mocks) ANTES de cada método @Test
// rodar, e valida ao final que nenhum stub foi criado e nunca usado (comportamento chamado de
// "strict stubbing", ligado por padrão nesta extensão). Sem esta anotação, os campos @Mock
// ficariam null.
// Paralelo .NET/Moq: equivalente ao `[TestClass]`/`MockRepository` do MSTest, ou simplesmente ao
// próprio `new Mock<T>()` do Moq — Moq não precisa de uma extensão de framework de teste porque
// não depende de bytecode/proxy gerado antes da execução do jeito que o JUnit 5 + Mockito fazem.
// -------------------------------------------------------------------------------------------
@ExtendWith(MockitoExtension.class)
class ProcessoServiceTest {

    // -----------------------------------------------------------------------------------------
    // @Mock — cria um "dublê de teste" (test double) para a interface: um objeto que IMPLEMENTA
    // ProcessoRepository/ProcessoMapper sem nenhuma lógica real por trás, até você programar
    // (`when(...).thenReturn(...)`) o que cada chamada deve devolver. Nenhum destes toca em banco
    // ou gera código MapStruct de verdade — é exatamente por isso que este teste roda em
    // milissegundos, sem subir Spring nem H2.
    // Paralelo .NET/Moq: `var mock = new Mock<IProcessoRepository>();`.
    // -----------------------------------------------------------------------------------------
    @Mock
    private ProcessoRepository processoRepository;

    @Mock
    private ProcessoMapper processoMapper;

    // A classe sob teste é instanciada manualmente (não injetada pelo Spring) — outra prova de
    // que este teste não precisa de contexto Spring nenhum. Repare que isso só é possível porque
    // ProcessoServiceImpl recebe suas dependências via CONSTRUTOR (ver docs/03) — se fosse
    // @Autowired em campo, não teríamos como passar os mocks aqui.
    private ProcessoServiceImpl processoService;

    @BeforeEach
    void setUp() {
        processoService = new ProcessoServiceImpl(processoRepository, processoMapper);
    }

    @Test
    void deveCriarProcessoComPartesEDelegarParaORepository() {
        // Arrange
        ParteRequestDTO parteDto = new ParteRequestDTO();
        parteDto.setNome("Igor Bonato");
        parteDto.setTipoParte(TipoParte.AUTOR);
        parteDto.setDocumento("12345678900");

        ProcessoRequestDTO dto = new ProcessoRequestDTO();
        dto.setNumero("0001");
        dto.setDataAutuacao(LocalDate.of(2026, 1, 1));
        dto.setPartes(Collections.singletonList(parteDto));

        ProcessoResponseDTO respostaEsperada = new ProcessoResponseDTO();
        // save() é um "identity function" simulada aqui: no Hibernate real, persist() (entidade
        // NOVA, sem id) devolve a MESMA instância — ver docs/09 sobre a diferença para merge().
        when(processoRepository.save(any(Processo.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(processoMapper.toResponseDTO(any(Processo.class))).thenReturn(respostaEsperada);

        // Act
        ProcessoResponseDTO resultado = processoService.criar(dto);

        // Assert
        assertThat(resultado).isSameAs(respostaEsperada);

        // ArgumentCaptor — "captura" o objeto de verdade que o service passou para save(), para
        // podermos inspecionar SEU ESTADO INTERNO (a Parte foi realmente adicionada?), algo que
        // um simples verify(...) não permite checar.
        // Paralelo .NET/Moq: `It.Is<Processo>(p => ...)` como matcher, ou capturar via
        // `mock.Invocations` — ArgumentCaptor é mais explícito para inspecionar depois do fato.
        ArgumentCaptor<Processo> captor = ArgumentCaptor.forClass(Processo.class);
        verify(processoRepository).save(captor.capture());
        Processo processoPersistido = captor.getValue();

        assertThat(processoPersistido.getNumero()).isEqualTo("0001");
        assertThat(processoPersistido.getPartes()).hasSize(1);
        assertThat(processoPersistido.getPartes().get(0).getNome()).isEqualTo("Igor Bonato");
    }

    @Test
    void deveBuscarProcessoPorIdQuandoExiste() {
        Processo processo = new Processo("0002", "Classe", "Vara", LocalDate.now());
        ProcessoResponseDTO respostaEsperada = new ProcessoResponseDTO();

        when(processoRepository.findById(1L)).thenReturn(Optional.of(processo));
        when(processoMapper.toResponseDTO(processo)).thenReturn(respostaEsperada);

        ProcessoResponseDTO resultado = processoService.buscarPorId(1L);

        assertThat(resultado).isSameAs(respostaEsperada);
    }

    @Test
    void deveLancarExcecaoAoBuscarProcessoInexistente() {
        when(processoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ProcessoNaoEncontradoException.class, () -> processoService.buscarPorId(99L));

        // O mapper nunca deveria ser chamado se o processo não foi encontrado — confirma que a
        // exceção é lançada ANTES de qualquer tentativa de mapear um objeto que não existe.
        verify(processoMapper, never()).toResponseDTO(any());
    }

    @Test
    void deveListarTodosQuandoStatusNaoInformado() {
        List<Processo> processos = Collections.singletonList(new Processo("0003", "C", "V", LocalDate.now()));
        when(processoRepository.findAll()).thenReturn(processos);
        when(processoMapper.toResponseDTOList(processos)).thenReturn(Collections.emptyList());

        processoService.listar(null);

        verify(processoRepository).findAll();
        // findByStatus NUNCA deveria ser chamado neste cenário — confirma que o "if" do service
        // realmente bifurca com base no parâmetro, e não chama os dois métodos "por garantia".
        verify(processoRepository, never()).findByStatus(any());
    }

    @Test
    void deveListarPorStatusQuandoInformado() {
        when(processoRepository.findByStatus(StatusProcesso.ARQUIVADO)).thenReturn(Collections.emptyList());
        when(processoMapper.toResponseDTOList(any())).thenReturn(Collections.emptyList());

        processoService.listar(StatusProcesso.ARQUIVADO);

        verify(processoRepository).findByStatus(StatusProcesso.ARQUIVADO);
        verify(processoRepository, never()).findAll();
    }

    @Test
    void deveArquivarProcessoExistente() {
        Processo processo = new Processo("0004", "C", "V", LocalDate.now());
        ProcessoResponseDTO respostaEsperada = new ProcessoResponseDTO();

        when(processoRepository.findById(1L)).thenReturn(Optional.of(processo));
        when(processoMapper.toResponseDTO(processo)).thenReturn(respostaEsperada);

        ProcessoResponseDTO resultado = processoService.arquivar(1L);

        assertThat(resultado).isSameAs(respostaEsperada);
        assertThat(processo.getStatus()).isEqualTo(StatusProcesso.ARQUIVADO);
        // Confirma, no nível do teste, a decisão documentada em ProcessoServiceImpl: nenhuma
        // chamada extra a save() depois de arquivar() — dirty checking cuida disso sozinho em
        // produção; aqui, sem um EntityManager de verdade, isso só reforça que o método não
        // tenta compensar a ausência dele com uma chamada redundante.
        verify(processoRepository, times(0)).save(any());
    }

    @Test
    void deveLancarExcecaoAoArquivarProcessoInexistente() {
        when(processoRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ProcessoNaoEncontradoException.class, () -> processoService.arquivar(1L));
    }
}
