package com.spring.gridfs.error;


import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.*;


@Component
public class CustomErrorAttributes extends DefaultErrorAttributes {

    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest webRequest, ErrorAttributeOptions options) {
        Map<String, Object> errorAttributes = new LinkedHashMap();

        Map<String, Object> attributes = super.getErrorAttributes(webRequest, options);

        long timestamp = ((Date) attributes.get("timestamp")).getTime();

        errorAttributes.put("status", attributes.get("status"));
        errorAttributes.put("message", attributes.get("message"));
        errorAttributes.put("path", attributes.get("path"));
        errorAttributes.put("timestamp", timestamp);

        if(attributes.containsKey("errors")){
            List<FieldError> fieldErrors = (List<FieldError>) attributes.get("errors");
            Map<String, String> errors = new HashMap<>();
            for(FieldError fieldError: fieldErrors){
                errors.put(fieldError.getField(),fieldError.getDefaultMessage());
            }
            errorAttributes.put("errors",errors);
        }

        return errorAttributes;

    }

}
