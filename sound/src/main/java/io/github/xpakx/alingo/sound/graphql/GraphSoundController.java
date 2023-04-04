package io.github.xpakx.alingo.sound.graphql;

import io.github.xpakx.alingo.sound.SoundService;
import io.github.xpakx.alingo.sound.dto.FilesResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@RequiredArgsConstructor
@Validated
public class GraphSoundController {
    private final SoundService service;

    @QueryMapping
    @Secured("MODERATOR")
    public FilesResponse getSounds(@Min(value = 1, message = "Page must be positive") @NotNull(message = "Page cannot be null") @Argument int page) {
        return service.getFileNames(page-1);
    }
}
