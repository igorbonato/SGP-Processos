package br.jus.trt4.processo.dto.response;

import java.time.LocalDateTime;

/**
 * Formato padrão de erro devolvido pelo {@code GlobalExceptionHandler} — construído à mão (não
 * vem de um mapper, não existe entidade "Erro" nenhuma), garantindo que toda falha tratada pela
 * API tenha a MESMA forma de payload, independente de qual exceção a originou.
 */
public class ErroResponseDTO {

    private final LocalDateTime timestamp;
    private final int status;
    private final String mensagem;

    public ErroResponseDTO(int status, String mensagem) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.mensagem = mensagem;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getMensagem() {
        return mensagem;
    }
}
