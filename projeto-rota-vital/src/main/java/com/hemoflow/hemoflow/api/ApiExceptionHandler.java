package com.hemoflow.hemoflow.api;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> validacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getFieldErrors().stream()
                .map(this::formatarCampo)
                .toList();
        return ResponseEntity.badRequest().body(new ErroResposta(
                LocalDateTime.now(),
                400,
                "Erro de validação",
                "Campo obrigatório ausente ou inválido",
                detalhes
        ));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> naoEncontrado(RecursoNaoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErroResposta(
                LocalDateTime.now(),
                404,
                "Recurso não encontrado",
                ex.getMessage(),
                List.of()
        ));
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> regra(RegraNegocioException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(new ErroResposta(
                LocalDateTime.now(),
                422,
                "Regra de negócio violada",
                ex.getMessage(),
                List.of()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> interno(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErroResposta(
                LocalDateTime.now(),
                500,
                "Erro interno",
                ex.getMessage(),
                List.of()
        ));
    }

    private String formatarCampo(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }

    public record ErroResposta(
            LocalDateTime timestamp,
            int status,
            String erro,
            String mensagem,
            List<String> detalhes
    ) {
    }
}
