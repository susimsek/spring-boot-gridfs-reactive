package com.spring.gridfs.error;


import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import org.zalando.problem.*;
import org.zalando.problem.spring.webflux.advice.ProblemHandling;
import org.zalando.problem.violations.ConstraintViolationProblem;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@ControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ExceptionTranslator implements ProblemHandling{
    
    final Environment environment;

    @Override
    public Mono<ResponseEntity<Problem>> process(@Nullable ResponseEntity<Problem> entity, ServerWebExchange request) {
        if (entity == null) {
            return Mono.empty();
        }
        Problem problem = entity.getBody();
        if (!(problem instanceof ConstraintViolationProblem || problem instanceof DefaultProblem)) {
            return Mono.just(entity);
        }
        ProblemBuilder builder = Problem.builder()
            .withType(Problem.DEFAULT_TYPE.equals(problem.getType()) ? URI.create(environment.getProperty("problem.default.type")) : problem.getType())
            .withStatus(problem.getStatus())
            .withTitle(problem.getTitle())
            .with(environment.getProperty("problem.path.key"), request.getRequest().getPath().value());

        if (problem instanceof ConstraintViolationProblem) {
            builder
                .with(environment.getProperty("problem.violations.key"), ((ConstraintViolationProblem) problem).getViolations())
                .with(environment.getProperty("problem.message.key"), environment.getProperty("problem.error.validation"));
        } else {
            builder
                .withCause(((DefaultProblem) problem).getCause())
                .withDetail(problem.getDetail())
                .withInstance(problem.getInstance());
            problem.getParameters().forEach(builder::with);
            if (!problem.getParameters().containsKey(environment.getProperty("problem.message.key")) && problem.getStatus() != null) {
                builder.with(environment.getProperty("problem.message.key"), "error.http." + problem.getStatus().getStatusCode());
            }
        }
        return Mono.just(new ResponseEntity<>(builder.build(), entity.getHeaders(), entity.getStatusCode()));
    }



    @Override
    public Mono<ResponseEntity<Problem>> handleBindingResult(WebExchangeBindException ex, @Nonnull ServerWebExchange request) {
        BindingResult result = ex.getBindingResult();
        List<FieldError> fieldErrors = result.getFieldErrors().stream()
            .map(f -> new FieldError(f.getObjectName().replaceFirst("Dto$", ""), f.getField(), f.getCode()))
            .collect(Collectors.toList());

        Problem problem = Problem.builder()
            .withType(URI.create(environment.getProperty("problem.constraint.violation.type")))
            .withTitle("Data binding and validation failure")
            .withStatus(Status.BAD_REQUEST)
            .with(environment.getProperty("problem.message.key"), environment.getProperty("problem.error.validation"))
            .with(environment.getProperty("problem.field.errors.key"), fieldErrors)
            .build();
        return create(ex, problem, request);
    }


    @Override
    public ProblemBuilder prepare(final Throwable throwable, final StatusType status, final URI type) {

            if (throwable instanceof HttpMessageConversionException) {
                return Problem.builder()
                    .withType(type)
                    .withTitle(status.getReasonPhrase())
                    .withStatus(status)
                    .withDetail("Unable to convert http message")
                    .withCause(Optional.ofNullable(throwable.getCause())
                        .filter(cause -> isCausalChainsEnabled())
                        .map(this::toProblem)
                        .orElse(null));
            }
            if (containsPackageName(throwable.getMessage())) {
                return Problem.builder()
                    .withType(type)
                    .withTitle(status.getReasonPhrase())
                    .withStatus(status)
                    .withDetail("Unexpected runtime exception")
                    .withCause(Optional.ofNullable(throwable.getCause())
                        .filter(cause -> isCausalChainsEnabled())
                        .map(this::toProblem)
                        .orElse(null));
            }

        return Problem.builder()
            .withType(type)
            .withTitle(status.getReasonPhrase())
            .withStatus(status)
            .withDetail(throwable.getMessage())
            .withCause(Optional.ofNullable(throwable.getCause())
                .filter(cause -> isCausalChainsEnabled())
                .map(this::toProblem)
                .orElse(null));
    }

    private boolean containsPackageName(String message) {

        // This list is for sure not complete
        return StringUtils.containsAny(message, "org.", "java.", "net.", "javax.", "com.", "io.", "de.", "com.spring.gridfs");
    }
}