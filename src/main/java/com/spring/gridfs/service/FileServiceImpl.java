package com.spring.gridfs.service;

import com.hanqf.reactive.redis.cache.aop.ReactiveRedisCacheEvict;
import com.hanqf.reactive.redis.cache.aop.ReactiveRedisCacheable;
import com.mongodb.BasicDBObject;
import com.spring.gridfs.exception.ResourceNotFoundException;
import com.spring.gridfs.model.FileResponseDto;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsOperations;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileServiceImpl implements FileService {

    final ReactiveGridFsOperations gridFsOperations;


    @ReactiveRedisCacheable(cacheName = "files", key = "all")
    @Override
    public Flux<FileResponseDto> getFiles(){
        Query query = query(where("metadata.type").is("file"));

        return gridFsOperations.find(query)
                .map(file -> FileResponseDto.builder()
                        .id(file.getObjectId().toHexString())
                        .name(file.getFilename())
                        .uploadedDate(file.getUploadDate())
                        .build());
    }

    @ReactiveRedisCacheEvict(cacheName = "files", key = "all")
    @Override
    public Mono<FileResponseDto> uploadFile(Mono<FilePart> filePart) {
        BasicDBObject object = new BasicDBObject();
        object.put("type","file");

         return filePart.flatMap(part ->
                 gridFsOperations.store(part.content(), part.filename(),object)
                 .map(objectId -> FileResponseDto.builder()
                         .id(objectId.toHexString())
                         .name(part.filename())
                         .uploadedDate(objectId.getDate())
                         .build())
         );
    }

    @Override
    public Mono<ReactiveGridFsResource> getFile(String id) {
        return gridFsOperations.findOne(query(where("_id").is(id)))
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("File", "id", id)))
                .log()
                .flatMap(gridFsOperations::getResource);
    }

    @ReactiveRedisCacheEvict(cacheName = "files", key = "all")
    @Override
    public Mono<Boolean> deleteFile(String id) {

        Query query = query(where("_id").is(id));

        return gridFsOperations.findOne(query)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("File", "id", id)))
                .log()
                .flatMap(file -> gridFsOperations.delete(query))
                .then(Mono.just(true));
    }
}
