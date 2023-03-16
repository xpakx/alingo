package io.github.xpakx.alingo.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
public class FileException extends RuntimeException {
    public FileException(String message) {
        super(message);
    }
    public FileException() {
        super("Operation failed!");
    }
}
