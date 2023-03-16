package io.github.xpakx.alingo.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UploadResponse {
    private List<String> files;
}
