package io.nexus.economizze.shared.exception;

import io.nexus.economizze.shared.dto.RespostaErroApi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Tratamento padronizado de erros para endpoints REST.
 */
@Slf4j
@RestControllerAdvice(basePackages = "io.nexus.economizze", annotations = RestController.class)
public class ApiExceptionHandler {

    /**
     * Trata erros de validacao de Bean Validation em requests.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaErroApi> tratarValidacao(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(this::formatarFieldError)
                .collect(Collectors.toList());

        RespostaErroApi resposta = new RespostaErroApi(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Dados invalidos",
                request.getRequestURI(),
                detalhes
        );

        return ResponseEntity.badRequest().body(resposta);
    }

    /**
     * Trata erros de validacao em parametros simples.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<RespostaErroApi> tratarConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        List<String> detalhes = ex.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .toList();

        RespostaErroApi resposta = new RespostaErroApi(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Violacao de validacao",
                request.getRequestURI(),
                detalhes
        );

        return ResponseEntity.badRequest().body(resposta);
    }

    /**
     * Trata recursos nao encontrados.
     */
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaErroApi> tratarNaoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest request) {
        RespostaErroApi resposta = new RespostaErroApi(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(resposta);
    }

    /**
     * Trata erros de regra de negocio e conflitos de consistencia.
     */
    @ExceptionHandler({RegraDeNegocioException.class, RecursoVinculadoException.class})
    public ResponseEntity<RespostaErroApi> tratarRegraNegocio(RuntimeException ex, HttpServletRequest request) {
        RespostaErroApi resposta = new RespostaErroApi(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI(),
                List.of()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
    }

    /**
     * Trata falhas inesperadas sem expor dados sensiveis.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErroApi> tratarErroGeral(Exception ex, HttpServletRequest request) {
        log.error("Erro inesperado na requisicao {}", request.getRequestURI(), ex);

        RespostaErroApi resposta = new RespostaErroApi(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ocorreu um erro inesperado no servidor",
                request.getRequestURI(),
                List.of()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
    }

    /**
     * Formata erro de campo para mensagem mais objetiva.
     */
    private String formatarFieldError(FieldError fieldError) {
        return fieldError.getField() + ": " + fieldError.getDefaultMessage();
    }
}
