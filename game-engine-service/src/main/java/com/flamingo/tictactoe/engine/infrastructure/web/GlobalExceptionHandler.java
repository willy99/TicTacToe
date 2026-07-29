package com.flamingo.tictactoe.engine.infrastructure.web;

import com.flamingo.tictactoe.engine.domain.exception.GameAlreadyFinishedException;
import com.flamingo.tictactoe.engine.domain.exception.GameNotFoundException;
import com.flamingo.tictactoe.engine.domain.exception.InvalidMoveException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates domain and validation exceptions into RFC 7807 {@link ProblemDetail}
 * responses, so every error the API returns has a consistent, machine-readable shape.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(GameNotFoundException.class)
    public ProblemDetail handleGameNotFound(GameNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Game Not Found", ex.getMessage());
    }

    @ExceptionHandler(GameAlreadyFinishedException.class)
    public ProblemDetail handleGameAlreadyFinished(GameAlreadyFinishedException ex) {
        return problem(HttpStatus.CONFLICT, "Game Already Finished", ex.getMessage());
    }

    @ExceptionHandler(InvalidMoveException.class)
    public ProblemDetail handleInvalidMove(InvalidMoveException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid Move", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Validation failed");
        return problem(HttpStatus.BAD_REQUEST, "Validation Error", detail);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid Request", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unexpected error handling request", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred");
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(title);
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
