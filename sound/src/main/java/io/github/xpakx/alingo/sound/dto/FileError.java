package io.github.xpakx.alingo.sound.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileError {
    private String filename;
    private String error;
}
