package imd.ufrn.com.br.smart_space_booking.instancia_sala.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Tratamento de exceções específicas do hotspot Sala — complementa o GlobalExceptionHandler do framework. */
@RestControllerAdvice
public class SalaExceptionHandler {

    @ExceptionHandler(SalaNotFoundException.class)
    public ResponseEntity<String> handleSalaNotFoundException(SalaNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(SalaIncorretaException.class)
    public ResponseEntity<String> handleSalaIncorretaException(SalaIncorretaException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getMessage());
    }
}
