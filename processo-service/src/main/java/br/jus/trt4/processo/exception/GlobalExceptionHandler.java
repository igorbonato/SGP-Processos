package br.jus.trt4.processo.exception;

import br.jus.trt4.processo.dto.response.ErroResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

// -------------------------------------------------------------------------------------------
// @RestControllerAdvice = @ControllerAdvice + @ResponseBody (mesma relação de
// @RestController = @Controller + @ResponseBody, ver ProcessoController). É um "interceptador
// global de exceções": qualquer exceção lançada de DENTRO de qualquer @RestController da
// aplicação (não só destes dois controllers, QUALQUER um) passa primeiro por aqui, se houver um
// @ExceptionHandler compatível com o tipo dela — antes de virar um HTTP 500 genérico.
//
// Paralelo .NET: um middleware de exceção global (`app.UseExceptionHandler(...)` ou um
// middleware customizado registrado no pipeline) — a diferença é que lá você normalmente escreve
// UM handler com um switch/if por tipo de exceção; aqui o Spring já roteia para o método certo
// baseado no tipo declarado em @ExceptionHandler, sem você escrever o "if" de despacho.
// -------------------------------------------------------------------------------------------
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProcessoNaoEncontradoException.class)
    public ResponseEntity<ErroResponseDTO> tratarProcessoNaoEncontrado(ProcessoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErroResponseDTO(HttpStatus.NOT_FOUND.value(), ex.getMessage()));
    }

    // 422 Unprocessable Entity (não 400): a requisição está bem-formada e passou na validação de
    // formato (Bean Validation) — o problema é uma regra de NEGÓCIO (processo arquivado), não um
    // erro de sintaxe/formato do payload. É a distinção HTTP correta entre "seu JSON está
    // malformado" (400) e "seu JSON é válido, mas o que você pediu viola uma regra" (422).
    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponseDTO> tratarRegraDeNegocio(RegraDeNegocioException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ErroResponseDTO(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage()));
    }

    // MethodArgumentNotValidException: exceção que o Spring lança sozinho quando um @Valid falha
    // (ver ProcessoController.criar). Sem este handler, ela ainda viraria HTTP 400 por padrão do
    // Spring — capturamos aqui só para garantir que a RESPOSTA tenha o MESMO formato
    // (ErroResponseDTO) que as outras falhas, em vez do formato genérico do Spring.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponseDTO> tratarValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErroResponseDTO(HttpStatus.BAD_REQUEST.value(), mensagem));
    }
}
