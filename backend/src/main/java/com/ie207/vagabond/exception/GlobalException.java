package com.ie207.vagabond.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalException {
    private ResponseEntity<ErrorDetails> buildErrorResponse(Exception ex, WebRequest req, HttpStatus status) {
        ErrorDetails err = new ErrorDetails(ex.getMessage(), req.getDescription(false), LocalDateTime.now());
        log.error("Exception occurred: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(err, status);
    }

    @ExceptionHandler(TourException.class)
    public ResponseEntity<ErrorDetails> handleTourException(TourException ue, WebRequest req) {
        return buildErrorResponse(ue, req, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TicketException.class)
    public ResponseEntity<ErrorDetails> handleTicketException(TicketException ue, WebRequest req) {
        return buildErrorResponse(ue, req, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(TourNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleTourNotFoundException(TourNotFoundException ex, WebRequest req) {
        return buildErrorResponse(ex, req, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(CityNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleCityNotFoundException(CityNotFoundException ex, WebRequest req) {
        return buildErrorResponse(ex, req, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TicketNotFoundException.class)
    public ResponseEntity<ErrorDetails> handleTicketNotFoundException(TicketNotFoundException ex, WebRequest req) {
        return buildErrorResponse(ex, req, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TicketNotInTourException.class)
    public ResponseEntity<ErrorDetails> handleTicketNotInTourException(TicketNotInTourException ex, WebRequest req) {
        ErrorDetails err = new ErrorDetails("Vé không có trong tour", req.getDescription(false), LocalDateTime.now());
        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorDetails> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException me){
        ErrorDetails err=new ErrorDetails(me.getBindingResult().getFieldError().getDefaultMessage(),"validation error", LocalDateTime.now());
        return new ResponseEntity<ErrorDetails>(err, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("message", "Endpoint not found");

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorDetails> handleRuntimeException(RuntimeException ex) {
        ErrorDetails err = new ErrorDetails("Lôi server", ex.getMessage(), LocalDateTime.now());
        return new ResponseEntity<ErrorDetails>(err, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDetails> otherExceptionHandler(Exception e, WebRequest req){
        ErrorDetails error=new ErrorDetails(e.getMessage(),req.getDescription(false),LocalDateTime.now());

        return new ResponseEntity<ErrorDetails>(error,HttpStatus.ACCEPTED);
    }
}
