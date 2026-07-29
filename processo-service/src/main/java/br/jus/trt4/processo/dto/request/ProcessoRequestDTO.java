package br.jus.trt4.processo.dto.request;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;

/**
 * Payload de criação de um {@link br.jus.trt4.processo.domain.Processo}.
 */
public class ProcessoRequestDTO {

    @NotBlank(message = "numero é obrigatório")
    @Size(max = 30)
    private String numero;

    @Size(max = 150)
    private String classeJudicial;

    @Size(max = 150)
    private String vara;

    @NotNull(message = "dataAutuacao é obrigatória")
    private LocalDate dataAutuacao;

    // ---------------------------------------------------------------------------------------
    // @NotEmpty (em vez de @NotNull): exige que a lista exista E tenha pelo menos 1 elemento —
    // um processo sem nenhuma parte não faz sentido de negócio.
    //
    // @Valid AQUI (dentro do campo, não só no parâmetro do controller) é o que faz a validação
    // "cascatear" para dentro de cada ParteRequestDTO da lista. Sem este @Valid específico, o
    // Spring validaria só que a lista existe e não é vazia, mas NÃO entraria em cada Parte para
    // checar @NotBlank/@NotNull dela.
    // Paralelo .NET: o model binding do ASP.NET Core já valida coleções aninhadas
    // automaticamente por padrão — este @Valid explícito por campo é uma peculiaridade do Bean
    // Validation que não tem paralelo direto (lá seria automático).
    // ---------------------------------------------------------------------------------------
    @NotEmpty(message = "processo precisa ter ao menos uma parte")
    @Valid
    private List<ParteRequestDTO> partes;

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

    public LocalDate getDataAutuacao() {
        return dataAutuacao;
    }

    public void setDataAutuacao(LocalDate dataAutuacao) {
        this.dataAutuacao = dataAutuacao;
    }

    public List<ParteRequestDTO> getPartes() {
        return partes;
    }

    public void setPartes(List<ParteRequestDTO> partes) {
        this.partes = partes;
    }
}
