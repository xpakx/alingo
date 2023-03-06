package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.Language;
import io.github.xpakx.alingo.game.LanguageService;
import io.github.xpakx.alingo.game.dto.LanguageRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@RequiredArgsConstructor
@Validated
public class GraphLanguageController {
    private final LanguageService service;

    @MutationMapping
    @Secured("MODERATOR")
    public Language addLanguage(@NotBlank(message = "Language name cannot be empty") @Argument String name) {
        return service.createLanguage(toRequest(name));
    }

    @MutationMapping
    @Secured("MODERATOR")
    public Language editLanguage(@NotNull(message = "Language id must be provided!") @Argument Long languageId,
                                 @NotBlank(message = "Language name cannot be empty") @Argument String name) {
        return service.editLanguage(languageId, toRequest(name));
    }

    private LanguageRequest toRequest(String name) {
        return new LanguageRequest(name);
    }
}
