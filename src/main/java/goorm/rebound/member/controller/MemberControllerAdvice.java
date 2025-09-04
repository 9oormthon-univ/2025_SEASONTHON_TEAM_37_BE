package goorm.rebound.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = MemberController.class)
public class MemberControllerAdvice {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgsExTo400(IllegalArgumentException e) {
        Map<String, Object> responseEntity = new HashMap<>();
        responseEntity.put("status", HttpStatus.BAD_REQUEST.value());
        responseEntity.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        responseEntity.put("message", e.getMessage());

        return new ResponseEntity<>(responseEntity, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidErr(MethodArgumentNotValidException e) {
        Map<String, Object> responseEntity = new HashMap<>();
        responseEntity.put("status", HttpStatus.BAD_REQUEST.value());
        responseEntity.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());

        Map<String, String> messages = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            messages.put(error.getField(), error.getDefaultMessage());
        });

        responseEntity.put("message",messages);

        return new ResponseEntity<>(responseEntity, HttpStatus.BAD_REQUEST);
    }
}
