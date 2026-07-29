package br.jus.trt4.processo.exception;

/** Lançada quando um {@code Processo} é buscado por id e não existe. Unchecked — mesmo racional
 *  documentado em {@link RegraDeNegocioException}. Tratada no {@code GlobalExceptionHandler},
 *  traduzida para HTTP 404. */
public class ProcessoNaoEncontradoException extends RuntimeException {

    public ProcessoNaoEncontradoException(Long id) {
        super("Processo não encontrado com id: " + id);
    }
}
