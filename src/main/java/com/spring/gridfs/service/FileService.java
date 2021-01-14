package com.spring.gridfs.service;

import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface FileService {

    Mono<Object> uploadFile(Mono<FilePart> file);
    Mono<ReactiveGridFsResource> getFile(String id);
    Mono<Void> deleteFile(String id);




}
