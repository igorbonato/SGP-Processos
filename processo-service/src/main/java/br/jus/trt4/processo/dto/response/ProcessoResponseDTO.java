package br.jus.trt4.processo.dto.response;

import br.jus.trt4.processo.domain.StatusProcesso;

import java.time.LocalDate;
import java.util.List;

/** JavaBean clássico — ver nota em {@link ParteResponseDTO}. */
public class ProcessoResponseDTO {

    private Long id;
    private String numero;
    private String classeJudicial;
    private String vara;
    private StatusProcesso status;
    private LocalDate dataAutuacao;
    // Preenchido automaticamente pelo MapStruct a partir de List<Parte> usando o ParteMapper
    // (declarado via @Mapper(uses = ParteMapper.class) em ProcessoMapper) — ver docs/09.
    private List<ParteResponseDTO> partes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getClasseJudicial() {
        return classeJudicial;
    }

    public void setClasseJudicial(String classeJudicial) {
        this.classeJudicial = classeJudicial;
    }

    public String getVara() {
        return vara;
    }

    public void setVara(String vara) {
        this.vara = vara;
    }

    public StatusProcesso getStatus() {
        return status;
    }

    public void setStatus(StatusProcesso status) {
        this.status = status;
    }

    public LocalDate getDataAutuacao() {
        return dataAutuacao;
    }

    public void setDataAutuacao(LocalDate dataAutuacao) {
        this.dataAutuacao = dataAutuacao;
    }

    public List<ParteResponseDTO> getPartes() {
        return partes;
    }

    public void setPartes(List<ParteResponseDTO> partes) {
        this.partes = partes;
    }
}
