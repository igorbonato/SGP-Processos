package br.jus.trt4.processo.dto.response;

import br.jus.trt4.processo.domain.TipoParte;

/**
 * Representação de uma Parte na resposta HTTP.
 *
 * ---------------------------------------------------------------------------------------------
 * NOTA HONESTA: a primeira versão deste DTO era imutável (só construtor, sem setters), na aposta
 * de que o MapStruct usaria "mapeamento por construtor" (recurso existente no MapStruct, mas que
 * depende de versão/circunstância) para casar os parâmetros com os getters da entidade. Na
 * prática, com mapstruct 1.3.1.Final (a versão fixada neste projeto, ver pom.xml raiz), isso
 * gerou erro de compilação: "does not have an accessible parameterless constructor" — ou seja, o
 * MapStruct insistiu na estratégia PADRÃO dele (construtor sem argumentos + setters), e não
 * caiu automaticamente para mapeamento por construtor aqui.
 *
 * Lição prática: comportamento de annotation processor é sensível à versão exata da lib — o jeito
 * certo de confirmar é COMPILAR, não assumir pela documentação geral. Corrigido usando o padrão
 * universalmente suportado (construtor sem argumentos + setters, JavaBean clássico) — ver
 * docs/09-notas-para-prova.md.
 * ---------------------------------------------------------------------------------------------
 */
public class ParteResponseDTO {

    private Long id;
    private String nome;
    private TipoParte tipoParte;
    private String documento;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
