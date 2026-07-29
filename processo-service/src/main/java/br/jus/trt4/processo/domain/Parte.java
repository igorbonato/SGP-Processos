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
import java.util.Objects;

/**
 * Pessoa física ou jurídica ligada a um {@link Processo} (autor ou réu). Entidade "filha" do
 * agregado — só existe vinculada a um processo, nunca sozinha (ver {@link Processo#adicionarParte}).
 */
@Entity
@Table(name = "parte")
public class Parte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_parte", nullable = false, length = 20)
    private TipoParte tipoParte;

    /** CPF (pessoa física) ou CNPJ (pessoa jurídica) — validação de formato fica para a Fase 3
     *  (Bean Validation nos DTOs de entrada), aqui é só persistência. */
    @Column(nullable = false, length = 20)
    private String documento;

    // ---------------------------------------------------------------------------------------
    // @ManyToOne — lado "muitos" do relacionamento: quem efetivamente possui a coluna de chave
    // estrangeira é ESTE lado (ver @JoinColumn abaixo), por isso o Processo usa "mappedBy" e este
    // lado não usa.
    //
    // fetch = FetchType.LAZY — a Parte só vai carregar o Processo associado do banco quando
    // alguém CHAMAR getProcesso() de fato (proxy do Hibernate), não automaticamente junto com a
    // Parte. Isso é uma mudança DELIBERADA do padrão: @ManyToOne é EAGER (carrega junto, sempre)
    // por padrão na especificação JPA — só @OneToMany/@ManyToMany são LAZY por padrão. Forçamos
    // LAZY aqui porque, na prática, quase nunca navegamos de Parte para Processo neste módulo (é
    // o caminho inverso que interessa), então carregar sempre seria desperdício de uma consulta.
    //
    // Paralelo EF Core: o EF Core faz o OPOSTO por padrão — TODA navegação é "lazy" só se você
    // habilitar explicitamente proxies (`UseLazyLoadingProxies()`); sem isso, EF Core não carrega
    // NADA automaticamente (nem List, nem referência única) a menos que você use `.Include()`
    // explicitamente. É o inverso do JPA, que carrega agressivamente por padrão e você precisa
    // "desligar" caso a caso.
    // ---------------------------------------------------------------------------------------
    @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn — declara a coluna de chave estrangeira física nesta tabela ("processo_id").
    // Paralelo EF Core: `.HasForeignKey("ProcessoId")` na Fluent API, ou a convenção automática
    // de nome "<NomeDaClasse>Id" que o EF Core já assume sem configuração nenhuma.
    @JoinColumn(name = "processo_id", nullable = false)
    private Processo processo;

    protected Parte() {
    }

    public Parte(String nome, TipoParte tipoParte, String documento) {
        this.nome = nome;
        this.tipoParte = tipoParte;
        this.documento = documento;
    }

    /**
     * Pacote-privado (sem modificador public/private) de propósito: só o próprio pacote
     * {@code domain} pode vincular uma Parte a um Processo — e quem faz isso é
     * {@link Processo#adicionarParte}, nunca código de fora chamando este método diretamente.
     * É uma forma de "encapsulamento de agregado" sem precisar de um módulo Java (JPMS) inteiro
     * só para isso.
     */
    void vincularProcesso(Processo processo) {
        this.processo = processo;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoParte getTipoParte() {
        return tipoParte;
    }

    public String getDocumento() {
        return documento;
    }

    public Processo getProcesso() {
        return processo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Parte)) {
            return false;
        }
        Parte parte = (Parte) o;
        return Objects.equals(documento, parte.documento);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documento);
    }
}
