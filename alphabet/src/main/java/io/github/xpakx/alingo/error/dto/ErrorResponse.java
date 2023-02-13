package io.github.xpakx.alingo.error.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private String message;
    private String status;
    private Integer error;
}