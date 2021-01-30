package com.spring.gridfs.model;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FileResponseDto {

    String id;

    String name;

    Date uploadedDate;
}
