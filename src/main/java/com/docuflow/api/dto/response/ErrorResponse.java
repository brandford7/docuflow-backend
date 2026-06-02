package com.docuflow.api.dto.response;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
     int status,
        String message,
        List<FieldError> errors,
        Instant timestamp,
        String path
   
) 
{

        public record FieldError(String field, String message) {
        }
        
        public static ErrorResponse of(int status, String message, String path) {
                return new ErrorResponse(status, message, List.of(), Instant.now(), path);

        }
        
        public static ErrorResponse of(int status, String message,List<FieldError> errors, String path)
  {
            return new ErrorResponse(status, message, errors, Instant.now(), path);
        }
    
}
