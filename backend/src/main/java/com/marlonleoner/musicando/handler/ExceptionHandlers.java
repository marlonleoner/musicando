package com.marlonleoner.musicando.handler;

import com.marlonleoner.musicando.domain.exception.BaseException;
import com.marlonleoner.musicando.dto.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Tratamento centralizado de erros da API REST.
 *
 * <p>Exceções do próprio Spring MVC (corpo inválido, método não suportado, rota inexistente etc.) são tratadas pela
 * classe base com o status HTTP adequado. Exceções de negócio usam o código definido em {@link BaseException}. Qualquer
 * outra exceção resulta em 500, sem expor detalhes internos ao cliente.</p>
 */
@Slf4j
@RestControllerAdvice
public class ExceptionHandlers extends ResponseEntityExceptionHandler {

    private static final String INTERNAL_ERROR_MESSAGE = "Internal server error";

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex) {
        return ResponseEntity.status(ex.getCode()).body(new ErrorResponse(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error while processing request", ex);
        final int status = HttpStatus.INTERNAL_SERVER_ERROR.value();

        return ResponseEntity.status(status).body(new ErrorResponse(status, INTERNAL_ERROR_MESSAGE));
    }
}
