package io.github.xpakx.alingo.game.error;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DataException extends RuntimeException {
    public DataException(String message) {
        super(message);
    }
    public DataException() {
        super("Data incorrect!");
    }
}
