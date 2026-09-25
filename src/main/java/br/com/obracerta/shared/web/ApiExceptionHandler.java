package br.com.obracerta.shared.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.NoSuchElementException;

/**
 * Traduz excecoes de dominio em respostas HTTP no formato ProblemDetail (RFC 9457).
 *
 * <p>Herdar de ResponseEntityExceptionHandler faz os erros de validacao (@Valid)
 * sairem no mesmo formato, com status 400. O front-end le sempre o campo "detail",
 * que continua em portugues porque e exibido ao usuario.
 */
@RestControllerAdvice
class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    ProblemDetail handleNotFound(NoSuchElementException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleBadRequest(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }
}
