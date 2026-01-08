package iuh.fit.se.phazelsound.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException e) {
        if (e.getMessage().contains("thao tác quá nhanh")) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Too Many Requests");
            error.put("message", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.TOO_MANY_REQUESTS);
        }

        return ResponseEntity.badRequest().body(e.getMessage());
    }
}