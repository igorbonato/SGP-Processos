package br.jus.trt4.processo.dto.request;

import br.jus.trt4.processo.domain.TipoParte;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Corpo de uma Parte dentro do payload de criação de um Processo (ver {@link ProcessoRequestDTO}).
 *
 * DTOs de REQUEST neste projeto são JavaBeans clássicos (construtor sem argumentos implícito +
 * setters), de propósito — é o formato que o Jackson (desserialização JSON) espera por padrão,
 * sem anotação extra nenhuma. Diferente das entidades de domínio (ver {@link
 * br.jus.trt4.processo.domain.Parte}), um DTO NÃO tem invariante para proteger — ele é
 * deliberadamente "anêmico", só carrega dado bruto do cliente até a validação (`@Valid` no
 * controller) e a construção da entidade de verdade no service.
 */
public class ParteRequestDTO {

    // ---------------------------------------------------------------------------------------
    // Bean Validation (spec javax.validation — Jakarta EE 8), implementada em runtime pelo
    // Hibernate Validator. O Spring dispara essas checagens quando o controller usa @Valid no
    // parâmetro do método, ANTES de qualquer código nosso rodar.
    // Paralelo .NET: DataAnnotations (`[Required]`, `[StringLength]`) + model binding automático.
    // ---------------------------------------------------------------------------------------
    @NotBlank(message = "nome é obrigatório")
    @Size(max = 150)
    private String nome;

    @NotNull(message = "tipoParte é obrigatório")
    private TipoParte tipoParte;

    @NotBlank(message = "documento é obrigatório")
    @Size(max = 20)
    private String documento;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public TipoParte getTipoParte() {
        return tipoParte;
    }

    public void setTipoParte(TipoParte tipoParte) {
        this.tipoParte = tipoParte;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }
}
