package br.jus.trt4.processo.dto.response;

import br.jus.trt4.processo.domain.TipoMovimentacao;

import java.time.LocalDateTime;

/** JavaBean clássico (construtor sem argumentos + setters) — ver nota em {@link ParteResponseDTO}
 *  sobre por que este é o formato que o MapStruct realmente usa por padrão. */
public class MovimentacaoResponseDTO {

    private Long id;
    private String descricao;
    private TipoMovimentacao tipoMovimentacao;
    private LocalDateTime dataMovimentacao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public TipoMovimentacao getTipoMovimentacao() {
        return tipoMovimentacao;
    }

    public void setTipoMovimentacao(TipoMovimentacao tipoMovimentacao) {
        this.tipoMovimentacao = tipoMovimentacao;
    }

    public LocalDateTime getDataMovimentacao() {
        return dataMovimentacao;
    }

    public void setDataMovimentacao(LocalDateTime dataMovimentacao) {
        this.dataMovimentacao = dataMovimentacao;
    }
}
