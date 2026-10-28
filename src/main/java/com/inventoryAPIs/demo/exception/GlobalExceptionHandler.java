package com.inventoryAPIs.demo.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.sqm.PathElementException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), messageFor(error));
        }

        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Validation failed");
        problem.setProperty("errors", errors);
        return problem;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Request body is missing or malformed (check JSON syntax and enum values)");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Invalid request parameter");
        problem.setProperty("errors", Map.of(ex.getName(), invalidValueMessage(ex.getValue(), ex.getRequiredType())));
        return problem;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PropertyReferenceException.class)
    public ProblemDetail handleUnknownSortProperty(PropertyReferenceException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Cannot sort by unknown field: " + ex.getPropertyName());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ProblemDetail handleInvalidQuery(InvalidDataAccessApiUsageException ex) {
        if (isInvalidSort(ex)) {
            return problem(HttpStatus.BAD_REQUEST, "Cannot sort by unknown field");
        }
        return handleUnexpected(ex);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate(DuplicateResourceException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ResourceInUseException.class)
    public ProblemDetail handleInUse(ResourceInUseException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(BusinessRuleException.class)
    public ProblemDetail handleBusinessRule(BusinessRuleException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(InsufficientStockException.class)
    public ProblemDetail handleInsufficientStock(InsufficientStockException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResource(NoResourceFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "No endpoint matches this URL");
    }

    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return problem(HttpStatus.METHOD_NOT_ALLOWED, "Method " + ex.getMethod() + " is not supported for this URL");
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(ConcurrencyFailureException.class)
    public ProblemDetail handleConcurrencyFailure(ConcurrencyFailureException ex) {
        return problem(HttpStatus.CONFLICT, "The resource was modified concurrently, please retry");
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        return problem(HttpStatus.CONFLICT, "Request conflicts with existing data");
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication(AuthenticationException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleInvalidRefreshToken(InvalidRefreshTokenException ex) {
        return problem(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return problem(HttpStatus.FORBIDDEN, "You are not allowed to perform this action");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
    }

    /**
     * A query parameter bound into a filter object that can't be converted (e.g. {@code ?type=BANANA})
     * gets a readable message instead of Spring's raw conversion error.
     */
    private String messageFor(FieldError error) {
        if (error.contains(TypeMismatchException.class)) {
            TypeMismatchException mismatch = error.unwrap(TypeMismatchException.class);
            return invalidValueMessage(mismatch.getValue(), mismatch.getRequiredType());
        }
        return error.getDefaultMessage();
    }

    /**
     * Hibernate rejects a well-formed but unknown field ({@code sort=foo}) with a PathElementException.
     * Spring Data rejects one that isn't a field name at all ({@code sort=["foo"]}) before the query is built,
     * and that exception has no more specific type, so it can only be recognised by its message.
     */
    private boolean isInvalidSort(InvalidDataAccessApiUsageException ex) {
        return ex.getMostSpecificCause() instanceof PathElementException
                || (ex.getMessage() != null && ex.getMessage().startsWith("Sort expression"));
    }

    /** Lists the allowed values for enums, since the client can't guess them otherwise. */
    private String invalidValueMessage(Object value, Class<?> requiredType) {
        String message = "Invalid value '" + value + "'";
        if (requiredType != null && requiredType.isEnum()) {
            String allowed = Arrays.stream(requiredType.getEnumConstants())
                    .map(Object::toString)
                    .collect(Collectors.joining(", "));
            return message + ". Allowed values: " + allowed;
        }
        if (requiredType != null) {
            return message + ". Expected a " + requiredType.getSimpleName();
        }
        return message;
    }

    private ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(status.getReasonPhrase());
        return problem;
    }
}
