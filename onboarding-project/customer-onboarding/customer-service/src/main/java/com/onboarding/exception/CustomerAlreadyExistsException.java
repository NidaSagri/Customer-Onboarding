// src/main/java/com/onboarding/exception/CustomerAlreadyExistsException.java
package com.onboarding.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// You can optionally add @ResponseStatus to automatically map this exception to an HTTP status
@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict is a great choice for this error
public class CustomerAlreadyExistsException extends RuntimeException {

    public CustomerAlreadyExistsException(String message) {
        super(message);
    }
}