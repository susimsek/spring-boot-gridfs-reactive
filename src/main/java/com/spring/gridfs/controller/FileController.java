package com.spring.gridfs.controller;

import com.spring.gridfs.service.FileService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/versions/1")
public class FileController {

    final FileService fileService;

    @PostMapping("/files")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Object> uploadFile(@RequestPart Mono<FilePart> file){
        return fileService.uploadFile(file);
    }

    @GetMapping("/files/{id}/view")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Void> viewFile(@PathVariable String id, ServerWebExchange exchange) {
        return fileService.getFile(id)
                .flatMapMany((resource) -> {
                    exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"");
                    return exchange.getResponse().writeWith(resource.getDownloadStream());
                });
    }

    @GetMapping("/files/{id}/download")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Void> downloadFile(@PathVariable String id, ServerWebExchange exchange) {
        return fileService.getFile(id)
                .flatMapMany((resource) -> {
                    exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");
                    return exchange.getResponse().writeWith(resource.getDownloadStream());
                });
    }

    @DeleteMapping("/files/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteFile(@PathVariable String id) {
        return fileService.deleteFile(id);
    }
}
