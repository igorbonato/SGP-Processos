package br.jus.trt4.processo.unit.domain;

import br.jus.trt4.processo.domain.Movimentacao;
import br.jus.trt4.processo.domain.Parte;
import br.jus.trt4.processo.domain.Processo;
import br.jus.trt4.processo.domain.StatusProcesso;
import br.jus.trt4.processo.domain.TipoMovimentacao;
import br.jus.trt4.processo.domain.TipoParte;
import br.jus.trt4.processo.exception.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * ---------------------------------------------------------------------------------------------
 * Teste de UNIDADE sem Mockito nenhum — de propósito.
 * ---------------------------------------------------------------------------------------------
 * {@link Processo} é uma classe Java comum, sem dependência de Spring, banco ou qualquer outro
 * bean — testá-la é só instanciar com "new" e chamar métodos, igual testaria qualquer classe de
 * domínio em C#. Mockito existe para SUBSTITUIR colaboradores externos (repository, mapper); uma
 * entidade de domínio rica não tem colaborador nenhum para substituir, então não precisa dele.
 * Ver docs/07-testes-e-qualidade.md — nem todo teste "unitário" precisa de mock.
 */
class ProcessoTest {

    @Test
    void deveNascerComStatusAtivo() {
        Processo processo = criarProcesso();

        assertThat(processo.getStatus()).isEqualTo(StatusProcesso.ATIVO);
        assertThat(processo.getPartes()).isEmpty();
        assertThat(processo.getMovimentacoes()).isEmpty();
    }

    @Test
    void deveAdicionarParteEVincularOProcesso() {
        Processo processo = criarProcesso();
        Parte parte = new Parte("Igor Bonato", TipoParte.AUTOR, "12345678900");

        processo.adicionarParte(parte);

        assertThat(processo.getPartes()).containsExactly(parte);
        // O vínculo bidirecional (ver Parte.vincularProcesso, pacote-privado) é justamente o que
        // adicionarParte garante — sem ele, o lado "dono" da FK (Parte.processo) ficaria null e o
        // Hibernate salvaria a linha sem a chave estrangeira preenchida.
        assertThat(parte.getProcesso()).isSameAs(processo);
    }

    @Test
    void deveAdicionarMovimentacaoQuandoProcessoEstaAtivo() {
        Processo processo = criarProcesso();
        Movimentacao movimentacao = new Movimentacao("Despacho inicial", TipoMovimentacao.DESPACHO,
                LocalDateTime.now());

        processo.adicionarMovimentacao(movimentacao);

        assertThat(processo.getMovimentacoes()).containsExactly(movimentacao);
        assertThat(movimentacao.getProcesso()).isSameAs(processo);
    }

    // ---------------------------------------------------------------------------------------
    // ESTE é o teste que mais importa neste arquivo: a regra de negócio central do módulo (ver
    // docs/01-arquitetura-geral.md) — processo arquivado não aceita novas movimentações. Se
    // alguém remover o "if" de dentro de Processo.adicionarMovimentacao no futuro, é este teste
    // que quebra e avisa.
    // ---------------------------------------------------------------------------------------
    @Test
    void deveRecusarMovimentacaoQuandoProcessoEstaArquivado() {
        Processo processo = criarProcesso();
        processo.arquivar();

        Movimentacao movimentacao = new Movimentacao("Tentativa tardia", TipoMovimentacao.DESPACHO,
                LocalDateTime.now());

        assertThrows(RegraDeNegocioException.class, () -> processo.adicionarMovimentacao(movimentacao));
        // E a movimentação rejeitada não deve ter sido adicionada de qualquer forma.
        assertThat(processo.getMovimentacoes()).isEmpty();
    }

    @Test
    void deveMudarStatusParaArquivadoAoArquivar() {
        Processo processo = criarProcesso();

        processo.arquivar();

        assertThat(processo.getStatus()).isEqualTo(StatusProcesso.ARQUIVADO);
    }

    @Test
    void doisProcessosComMesmoNumeroDevemSerIguais() {
        Processo processo1 = new Processo("0001", "Classe", "Vara", LocalDate.now());
        Processo processo2 = new Processo("0001", "Classe Diferente", "Outra Vara", LocalDate.now().minusDays(10));

        // equals/hashCode são baseados no "numero" (chave de negócio) — ver o porquê no
        // comentário de Processo.java e em docs/09-notas-para-prova.md.
        assertThat(processo1).isEqualTo(processo2);
        assertThat(processo1.hashCode()).isEqualTo(processo2.hashCode());
    }

    private Processo criarProcesso() {
        return new Processo("0001234-56.2026.5.04.0001", "Reclamação Trabalhista",
                "1ª Vara do Trabalho de Porto Alegre", LocalDate.of(2026, 1, 15));
    }
}
