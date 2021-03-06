package com.spring.gridfs.service;

import com.spring.gridfs.model.FileResponseDto;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FileService {

    Mono<FileResponseDto> uploadFile(Mono<FilePart> file);
    Mono<ReactiveGridFsResource> getFile(String id);
    Mono<Boolean> deleteFile(String id);
    Flux<FileResponseDto> getFiles();
}
