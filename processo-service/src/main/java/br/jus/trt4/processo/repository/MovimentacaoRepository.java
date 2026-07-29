package br.jus.trt4.processo.repository;

import br.jus.trt4.processo.domain.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentacaoRepository extends JpaRepository<Movimentacao, Long> {

    // "OrderByDataMovimentacaoDesc" também é derivado do nome do método — Spring Data entende o
    // sufixo "OrderBy<Campo><Asc|Desc>" e gera o "ORDER BY data_movimentacao DESC" sozinho.
    // Equivalente EF Core/LINQ:
    // `_context.Movimentacoes.Where(m => m.Processo.Id == processoId)
    //                        .OrderByDescending(m => m.DataMovimentacao).ToListAsync()`.
    List<Movimentacao> findByProcessoIdOrderByDataMovimentacaoDesc(Long processoId);
}
