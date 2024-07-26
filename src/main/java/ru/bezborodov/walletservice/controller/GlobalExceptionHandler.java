package ru.bezborodov.walletservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;
import ru.bezborodov.walletservice.exception.InsufficientFundsException;
import ru.bezborodov.walletservice.exception.WalletNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ProblemDetail>> handleWebExchangeBindException(WebExchangeBindException exception) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setProperty("errors", exception.getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .toList());

        return Mono.just(ResponseEntity.badRequest()
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problemDetail));
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public Mono<ResponseEntity<String>> handleWalletNotFoundException(WalletNotFoundException ex) {
        return Mono.just(new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public Mono<ResponseEntity<String>> handleInsufficientFundsException(InsufficientFundsException ex) {
        return Mono.just(new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<String>> handleException(Exception ex) {
        return Mono.just(new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Mono.just(new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }
}
