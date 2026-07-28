package com.flamingo.tictactoe.session.infrastructure.web;

import com.flamingo.tictactoe.session.domain.exception.GameEngineCommunicationException;
import com.flamingo.tictactoe.session.domain.exception.SessionAlreadyCompletedException;
import com.flamingo.tictactoe.session.domain.exception.SessionNotFoundException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Translates domain exceptions into RFC 7807 {@link ProblemDetail} responses,
 * so every error the API returns has a consistent, machine-readable shape.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SessionNotFoundException.class)
    public ProblemDetail handleSessionNotFound(SessionNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Session Not Found", ex.getMessage());
    }

    @ExceptionHandler(SessionAlreadyCompletedException.class)
    public ProblemDetail handleSessionAlreadyCompleted(SessionAlreadyCompletedException ex) {
        return problem(HttpStatus.CONFLICT, "Session Already Completed", ex.getMessage());
    }

    @ExceptionHandler(GameEngineCommunicationException.class)
    public ProblemDetail handleGameEngineCommunication(GameEngineCommunicationException ex) {
        return problem(HttpStatus.BAD_GATEWAY, "Game Engine Communication Error", ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
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
