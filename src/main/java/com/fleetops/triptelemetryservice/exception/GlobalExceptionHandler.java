package com.fleetops.triptelemetryservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
@Order(-1)
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Not found: {}", ex.getMessage());
        return problemResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<ProblemDetail> handleInvalidState(InvalidStateTransitionException ex, HttpServletRequest request) {
        log.warn("Invalid state transition: {}", ex.getMessage());
        return problemResponse(HttpStatus.CONFLICT, "Invalid Operation", ex.getMessage(), request);
    }

    @ExceptionHandler(OrderDispatchServiceException.class)
    public ResponseEntity<ProblemDetail> handleOrderDispatchError(OrderDispatchServiceException ex, HttpServletRequest request) {
        log.error("Order dispatch service error: {}", ex.getMessage(), ex);
        return problemResponse(HttpStatus.BAD_GATEWAY, "Order Dispatch Service Error",
                "Unable to update order status. Please try again later.", request);
    }

    @ExceptionHandler(NotificationServiceException.class)
    public ResponseEntity<ProblemDetail> handleNotificationError(NotificationServiceException ex, HttpServletRequest request) {
        log.error("Notification service error: {}", ex.getMessage(), ex);
        return problemResponse(HttpStatus.BAD_GATEWAY, "Notification Service Error",
                "Unable to send notification.", request);
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ProblemDetail> handleBindException(BindException ex, HttpServletRequest request) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        log.warn("Validation failed for [{}]: {}", request.getRequestURI(), detail);
        ProblemDetail problem = build(HttpStatus.UNPROCESSABLE_ENTITY, "Validation Error", detail, request);
        ex.getBindingResult().getFieldErrors().forEach(fe -> problem.setProperty(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String detail = ex.getConstraintViolations().stream()
                .map(cv -> {
                    String path = cv.getPropertyPath().toString();
                    String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                    return field + ": " + cv.getMessage();
                })
                .collect(Collectors.joining("; "));
        log.warn("Constraint violation for [{}]: {}", request.getRequestURI(), detail);
        return problemResponse(HttpStatus.UNPROCESSABLE_ENTITY, "Validation Error", detail, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on [{}]: {}", request.getRequestURI(), ex.getMessage(), ex);
        return problemResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.", request);
    }

    private ResponseEntity<ProblemDetail> problemResponse(HttpStatus status, String title, String detail, HttpServletRequest request) {
        ProblemDetail problem = build(status, title, detail, request);
        return ResponseEntity.status(status).contentType(MediaType.APPLICATION_PROBLEM_JSON).body(problem);
    }

    private ProblemDetail build(HttpStatus status, String title, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }
}