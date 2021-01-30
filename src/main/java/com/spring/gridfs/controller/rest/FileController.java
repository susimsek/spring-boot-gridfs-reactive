package com.spring.gridfs.controller.rest;

import com.spring.gridfs.model.FileResponseDto;
import com.spring.gridfs.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@RequestMapping("/versions/1")
@Tag(name = "files", description = "Retrieve and manage files")
public class FileController {

    final FileService fileService;

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok",content = @Content(array = @ArraySchema(schema = @Schema(implementation = FileResponseDto.class)))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",content = @Content)

    })
    @Operation(summary = "Get all Files")
    @GetMapping(value = "/files" )
    @ResponseStatus(HttpStatus.OK)
    public Flux<FileResponseDto> getFiles(){
        return fileService.getFiles();
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok",content = @Content(schema = @Schema(implementation = FileResponseDto.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",content = @Content)

    })
    @Operation(summary = "Upload new File")
    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    @ResponseStatus(HttpStatus.OK)
    public Mono<FileResponseDto> uploadFile(@RequestPart(name = "file") @Parameter(description = "File to be uploaded", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)) Mono<FilePart> file){
        return fileService.uploadFile(file);
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok",content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found",content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",content = @Content)

    })
    @Operation(summary = "View existing File")
    @GetMapping("/files/{fileId}/view")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Void> viewFile(
            @Parameter(description="Id of the File", required=true) @PathVariable String fileId,
            ServerWebExchange exchange) {
        return fileService.getFile(fileId)
                .flatMapMany((resource) -> {
                    exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"");
                    return exchange.getResponse().writeWith(resource.getDownloadStream());
                });
    }

    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ok",content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found",content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",content = @Content)

    })
    @Operation(summary = "Download existing File")
    @GetMapping("/files/{fileId}/download")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Void> downloadFile(@Parameter(description="Id of the File", required=true) @PathVariable String fileId,
                                   ServerWebExchange exchange) {
        return fileService.getFile(fileId)
                .flatMapMany((resource) -> {
                    exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"");
                    return exchange.getResponse().writeWith(resource.getDownloadStream());
                });
    }

    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No Content",content = @Content),
            @ApiResponse(responseCode = "404", description = "Not Found",content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",content = @Content)

    })
    @Operation(summary = "Delete existing File")
    @DeleteMapping("/files/{fileId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Boolean> deleteFile(@Parameter(description="Id of the File", required=true) @PathVariable String fileId){
        return fileService.deleteFile(fileId);
    }
}
