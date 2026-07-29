package br.jus.trt4.processo.repository;

import br.jus.trt4.processo.domain.Parte;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ParteRepository extends JpaRepository<Parte, Long> {

    // "processo.id" navega o relacionamento @ManyToOne de Parte -> Processo dentro do nome do
    // método: o Spring Data entende o "." como "atravesse a associação e filtre pelo id dela".
    // Equivalente ao SQL "SELECT * FROM parte WHERE processo_id = ?", ou, em LINQ/EF Core,
    // `_context.Partes.Where(p => p.Processo.Id == processoId)`.
    List<Parte> findByProcessoId(Long processoId);
}
