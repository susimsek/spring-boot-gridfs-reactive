package com.spring.gridfs.controller.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

import java.net.URI;

@Controller
public class WebController {

    @GetMapping
    public Mono<Void> indexController(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
        response.getHeaders().setLocation(URI.create("/api/swagger-ui.html"));
        return response.setComplete();
    }
}