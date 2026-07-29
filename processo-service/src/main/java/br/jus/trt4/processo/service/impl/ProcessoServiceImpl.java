package br.jus.trt4.processo.service.impl;

import br.jus.trt4.processo.domain.Parte;
import br.jus.trt4.processo.domain.Processo;
import br.jus.trt4.processo.domain.StatusProcesso;
import br.jus.trt4.processo.dto.request.ParteRequestDTO;
import br.jus.trt4.processo.dto.request.ProcessoRequestDTO;
import br.jus.trt4.processo.dto.response.ProcessoResponseDTO;
import br.jus.trt4.processo.exception.ProcessoNaoEncontradoException;
import br.jus.trt4.processo.mapper.ProcessoMapper;
import br.jus.trt4.processo.repository.ProcessoRepository;
import br.jus.trt4.processo.service.ProcessoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// -------------------------------------------------------------------------------------------
// @Service — especialização de @Component (ver docs/03) para a camada de regra de negócio.
// Funcionalmente idêntica a @Component (o Spring trata ambas igual no container de DI); a
// diferença é só semântica/documental — sinaliza "esta classe é uma regra de negócio", útil para
// ferramentas de análise e para quem lê o código.
// Paralelo .NET: registrar a classe com `builder.Services.AddScoped<IProcessoService,
// ProcessoServiceImpl>()` — só que aqui o registro "acontece sozinho" via @ComponentScan (ver
// EurekaServerApplication.java), sem uma linha de bootstrap explícita.
// -------------------------------------------------------------------------------------------
@Service
public class ProcessoServiceImpl implements ProcessoService {

    private final ProcessoRepository processoRepository;
    private final ProcessoMapper processoMapper;

    // -----------------------------------------------------------------------------------------
    // Injeção via CONSTRUTOR (não @Autowired em campo) — permite que os campos sejam "final"
    // (imutáveis após construção) e torna a classe testável sem Spring nenhum: em um teste
    // unitário (Fase 7), basta chamar "new ProcessoServiceImpl(mockRepo, mockMapper)"
    // diretamente. Como há só UM construtor, o Spring nem precisa de @Autowired aqui — ele já
    // assume que este é o construtor a usar para injeção. Paralelo .NET: injeção via construtor
    // é o padrão recomendado há anos no ASP.NET Core também — mesma prática, mesmo motivo.
    // -----------------------------------------------------------------------------------------
    public ProcessoServiceImpl(ProcessoRepository processoRepository, ProcessoMapper processoMapper) {
        this.processoRepository = processoRepository;
        this.processoMapper = processoMapper;
    }

    @Override
    // -----------------------------------------------------------------------------------------
    // @Transactional (org.springframework.transaction.annotation, NÃO javax.transaction) — abre
    // uma transação de banco ao entrar no método e faz commit ao sair com sucesso (ou rollback se
    // uma RuntimeException escapar). É AQUI, dentro da transação, que a conversão para DTO
    // precisa acontecer (processoMapper.toResponseDTO no fim do método) — porque
    // "open-in-view: false" (ver application.yml e docs/09) fecha a sessão do Hibernate assim
    // que o método termina; se a conversão para DTO (que acessa "partes", uma coleção LAZY)
    // acontecesse só no controller, depois do método já ter retornado, estouraria
    // LazyInitializationException.
    // Paralelo .NET/EF Core: comparável a manter o `DbContext` vivo durante todo o método do
    // service e projetar para o DTO antes de retornar — só que aqui é o Spring quem abre/fecha a
    // "sessão" via esta anotação, não um `using var context = ...` manual.
    // -----------------------------------------------------------------------------------------
    @Transactional
    public ProcessoResponseDTO criar(ProcessoRequestDTO dto) {
        // Construção do agregado feita EXPLICITAMENTE aqui, não por um mapper — ver o comentário
        // completo em ProcessoMapper sobre por que "DTO -> Entity" nunca passa por MapStruct
        // neste projeto.
        Processo processo = new Processo(dto.getNumero(), dto.getClasseJudicial(), dto.getVara(),
                dto.getDataAutuacao());

        for (ParteRequestDTO parteDto : dto.getPartes()) {
            Parte parte = new Parte(parteDto.getNome(), parteDto.getTipoParte(), parteDto.getDocumento());
            // adicionarParte (método de domínio) garante o vínculo bidirecional correto — ver
            // Processo.java.
            processo.adicionarParte(parte);
        }

        // cascade = CascadeType.ALL (ver Processo.java, campo "partes") faz este único save()
        // persistir o Processo E todas as Partes recém-adicionadas na mesma operação — não
        // precisamos de um parteRepository.save() separado para cada uma.
        Processo salvo = processoRepository.save(processo);
        return processoMapper.toResponseDTO(salvo);
    }

    @Override
    // readOnly = true: dica de otimização para o Hibernate (desliga o "dirty checking" — ver
    // arquivar() abaixo — para esta transação, já que não vamos alterar nada) e para o driver JDBC
    // encaminhar a conexão como somente-leitura quando o banco suportar. Não é apenas
    // documentação: tem efeito real de performance em consultas.
    @Transactional(readOnly = true)
    public ProcessoResponseDTO buscarPorId(Long id) {
        Processo processo = buscarEntidadePorId(id);
        return processoMapper.toResponseDTO(processo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProcessoResponseDTO> listar(StatusProcesso status) {
        List<Processo> processos = (status != null)
                ? processoRepository.findByStatus(status)
                : processoRepository.findAll();
        return processoMapper.toResponseDTOList(processos);
    }

    @Override
    @Transactional
    public ProcessoResponseDTO arquivar(Long id) {
        Processo processo = buscarEntidadePorId(id);
        processo.arquivar();
        // -------------------------------------------------------------------------------------
        // SEM processoRepository.save(processo) aqui — e isso é DE PROPÓSITO, não esquecimento.
        // Dentro de uma transação, uma entidade carregada via findById() fica "managed"
        // (gerenciada) pelo Hibernate: qualquer mudança de estado nela (o arquivar() acima) é
        // detectada automaticamente por "DIRTY CHECKING" e traduzida em um UPDATE no COMMIT da
        // transação, sem precisarmos chamar save() de novo. Chamar save() aqui não seria
        // ERRADO (é um no-op nesse caso, já que a entidade já é gerenciada), mas é redundante.
        // Paralelo EF Core: equivalente ao Change Tracker do EF Core detectando que uma
        // propriedade de uma entidade rastreada mudou e gerando o UPDATE no
        // `SaveChanges()`/`SaveChangesAsync()` — só que no EF Core você AINDA precisa chamar
        // SaveChanges() explicitamente; aqui, quem "faz o SaveChanges" é o COMMIT automático da
        // transação do Spring ao sair do método.
        // -------------------------------------------------------------------------------------
        return processoMapper.toResponseDTO(processo);
    }

    private Processo buscarEntidadePorId(Long id) {
        return processoRepository.findById(id)
                .orElseThrow(() -> new ProcessoNaoEncontradoException(id));
    }
}
