package br.jus.trt4.processo.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Um item do histórico de um {@link Processo} (despacho, decisão, sentença). Assim como
 * {@link Parte}, é entidade filha do agregado — nasce sempre vinculada via
 * {@link Processo#adicionarMovimentacao}, nunca criada solta.
 *
 * Modelada como imutável de propósito: uma movimentação já registrada não deveria ser editada
 * (é histórico jurídico), só adicionada — por isso não há setters aqui, nem para os campos
 * próprios nem para o vínculo (fora do pacote-privado {@link #vincularProcesso}).
 */
@Entity
@Table(name = "movimentacao")
public class Movimentacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_movimentacao", nullable = false, length = 20)
    private TipoMovimentacao tipoMovimentacao;

    @Column(name = "data_movimentacao", nullable = false)
    private LocalDateTime dataMovimentacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "processo_id", nullable = false)
    private Processo processo;

    protected Movimentacao() {
    }

    public Movimentacao(String descricao, TipoMovimentacao tipoMovimentacao, LocalDateTime dataMovimentacao) {
        this.descricao = descricao;
        this.tipoMovimentacao = tipoMovimentacao;
        this.dataMovimentacao = dataMovimentacao;
    }

    /** Ver {@link Parte#vincularProcesso} — mesmo racional de encapsulamento pacote-privado. */
    void vincularProcesso(Processo processo) {
        this.processo = processo;
    }

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public TipoMovimentacao getTipoMovimentacao() {
        return tipoMovimentacao;
    }

    public LocalDateTime getDataMovimentacao() {
        return dataMovimentacao;
    }

    public Processo getProcesso() {
        return processo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Movimentacao)) {
            return false;
        }
        Movimentacao that = (Movimentacao) o;
        return Objects.equals(descricao, that.descricao)
                && Objects.equals(dataMovimentacao, that.dataMovimentacao);
    }

    @Override
    public int hashCode() {
        return Objects.hash(descricao, dataMovimentacao);
    }
}
