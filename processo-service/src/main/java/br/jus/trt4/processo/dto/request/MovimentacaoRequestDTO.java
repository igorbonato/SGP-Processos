package br.jus.trt4.processo.dto.request;

import br.jus.trt4.processo.domain.TipoMovimentacao;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Payload para adicionar uma {@link br.jus.trt4.processo.domain.Movimentacao} a um processo
 * existente. Repare que NÃO há campo "dataMovimentacao" aqui — o timestamp é decidido pelo
 * SERVIÇO (momento em que a movimentação é registrada), não pelo cliente da API. Ver
 * {@code MovimentacaoServiceImpl}.
 */
public class MovimentacaoRequestDTO {

    @NotBlank(message = "descricao é obrigatória")
    @Size(max = 500)
    private String descricao;

    @NotNull(message = "tipoMovimentacao é obrigatório")
    private TipoMovimentacao tipoMovimentacao;

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
}
