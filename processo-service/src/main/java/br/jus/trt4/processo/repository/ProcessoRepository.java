package br.jus.trt4.processo.repository;

import br.jus.trt4.processo.domain.Processo;
import br.jus.trt4.processo.domain.StatusProcesso;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// -------------------------------------------------------------------------------------------
// Repository de Processo — repare que isto é uma INTERFACE, sem nenhuma implementação escrita
// por nós. O Spring Data JPA gera uma implementação de verdade em runtime (via proxy dinâmico) e
// registra como um bean Spring, pronto para @Autowired em qualquer service.
//
// JpaRepository<Processo, Long> já entrega de graça, sem escrever uma linha: save(), findById(),
// findAll(), delete(), count(), além de paginação/ordenação prontas (findAll(Pageable)).
//
// Paralelo .NET: mistura de duas coisas que no EF Core normalmente vêm separadas —
//   1) O `DbSet<Processo>` do seu DbContext, que já dá Add/Find/Remove/etc.
//   2) O Repository Pattern que muita gente escreve MANUALMENTE em cima do EF Core
//      (`IProcessoRepository` + `ProcessoRepository : IProcessoRepository`) para não vazar
//      `DbContext` para as camadas de cima.
// Aqui as duas coisas colapsam em uma interface só, gerada pelo framework — você nunca escreve
// a classe de implementação.
// -------------------------------------------------------------------------------------------
public interface ProcessoRepository extends JpaRepository<Processo, Long> {

    // -----------------------------------------------------------------------------------------
    // QUERY METHOD — o Spring Data JPA lê o NOME do método e deriva a consulta SQL sozinho, sem
    // anotação nenhuma. "findByNumero" vira, por baixo dos panos, algo equivalente a
    // "SELECT * FROM processo WHERE numero = ?".
    //
    // Retornar Optional<Processo> (em vez de Processo, que poderia ser null) é convenção
    // recomendada do Spring Data desde o Java 8 — força quem chama a lidar explicitamente com o
    // caso "não encontrado" (.orElseThrow(...), .isPresent()...), em vez de arriscar um
    // NullPointerException silencioso.
    //
    // Paralelo .NET/EF Core: equivalente a escrever
    // `_context.Processos.FirstOrDefaultAsync(p => p.Numero == numero)` manualmente — aqui você
    // só declara a ASSINATURA do método, com o nome seguindo a convenção, e o SQL nasce sozinho.
    // -----------------------------------------------------------------------------------------
    Optional<Processo> findByNumero(String numero);

    // Outro query method derivado: "SELECT * FROM processo WHERE status = ?".
    // Paralelo EF Core: `_context.Processos.Where(p => p.Status == status).ToListAsync()`.
    List<Processo> findByStatus(StatusProcesso status);
}
