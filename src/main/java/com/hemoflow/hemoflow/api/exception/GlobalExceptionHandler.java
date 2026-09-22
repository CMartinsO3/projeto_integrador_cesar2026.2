package com.hemoflow.hemoflow.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErroResposta> handleRuntimeException(RuntimeException ex) {
        ErroResposta erro = new ErroResposta(
                LocalDateTime.now(),
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Regra de negócio violada",
                ex.getMessage(),
                new ArrayList<>()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> detalhes = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            detalhes.add(error.getField() + ": " + error.getDefaultMessage());
        }
        
        ErroResposta erro = new ErroResposta(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação",
                "Um ou mais campos estão inválidos",
                detalhes
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }
    
    public record ErroResposta(
            LocalDateTime timestamp,
            int status,
            String erro,
            String mensagem,
            List<String> detalhes
    ) {}
}
