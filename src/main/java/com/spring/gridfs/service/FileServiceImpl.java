package com.spring.gridfs.service;

import com.spring.gridfs.exception.ResourceNotFoundException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsOperations;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Map;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileServiceImpl implements FileService {

    final ReactiveGridFsOperations gridFsOperations;

    @Override
    public Mono<Object> uploadFile(Mono<FilePart> filePart) {
        return filePart.flatMap(part -> gridFsOperations.store(part.content(), part.filename()))
                .map((id) -> Map.of("id", id.toHexString()));
    }

    @Override
    public Mono<ReactiveGridFsResource> getFile(String id) {
        return gridFsOperations.findOne(query(where("_id").is(id)))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("File", "id", id)))
                .log()
                .flatMap(gridFsOperations::getResource);
    }

    @Override
    public Mono<Void> deleteFile(String id) {

        Query query = query(where("_id").is(id));

        return gridFsOperations.findOne(query)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("File", "id", id)))
                .log()
                .flatMap(file -> gridFsOperations.delete(query));
    }
}
