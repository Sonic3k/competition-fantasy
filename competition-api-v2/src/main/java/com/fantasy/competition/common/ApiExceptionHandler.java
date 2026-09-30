package com.fantasy.competition.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/** RFC 9457 problem details for every API error. */
@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    ProblemDetail notFound(NotFoundException e) {
        return problem(HttpStatus.NOT_FOUND, e.getMessage(), List.of());
    }

    @ExceptionHandler(BadRequestException.class)
    ProblemDetail badRequest(BadRequestException e) {
        return problem(HttpStatus.BAD_REQUEST, e.getMessage(), e.getDetails());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail illegalArgument(IllegalArgumentException e) {
        return problem(HttpStatus.BAD_REQUEST, e.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail invalid(MethodArgumentNotValidException e) {
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .toList();
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", details);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail integrity(DataIntegrityViolationException e) {
        String root = e.getMostSpecificCause() == null ? e.getMessage() : e.getMostSpecificCause().getMessage();
        return problem(HttpStatus.CONFLICT, "Data integrity violation", List.of(root == null ? "" : root));
    }

    private static ProblemDetail problem(HttpStatus status, String detail, List<String> details) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        if (details != null && !details.isEmpty()) pd.setProperty("details", details);
        return pd;
    }
}
