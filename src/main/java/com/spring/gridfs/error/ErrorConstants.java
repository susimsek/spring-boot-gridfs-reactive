package com.spring.gridfs.error;

import lombok.NoArgsConstructor;

import java.net.URI;

@NoArgsConstructor
public final class ErrorConstants {

    public static final String ERR_VALIDATION = "error.validation";
    public static final String PROBLEM_BASE_URL = "https://www.spring.test/problem";
    public static final URI DEFAULT_TYPE = URI.create(PROBLEM_BASE_URL + "/problem-with-message");
    public static final URI CONSTRAINT_VIOLATION_TYPE = URI.create(PROBLEM_BASE_URL + "/constraint-violation");
}
