package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.Language;
import io.github.xpakx.alingo.game.LanguageService;
import io.github.xpakx.alingo.game.dto.CourseDataDto;
import io.github.xpakx.alingo.game.dto.CourseForListDto;
import io.github.xpakx.alingo.game.dto.CourseList;
import io.github.xpakx.alingo.game.dto.LanguageRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.util.List;

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

    @QueryMapping
    @Secured("MODERATOR")
    public Language getLanguage(@NotNull(message = "Language id must be provided!") @Argument Long id) {
        return service.getLanguage(id);
    }

    @QueryMapping
    @Secured("MODERATOR")
    public List<Language> getLanguages(@Min(value = 1, message = "Page must be positive") @NotNull(message = "Page cannot be null") @Argument int page,
                                       @NotNull @Min(value = 1, message = "Amount must be between 1 and 20") @Max(value = 20, message = "Amount must be between 1 and 20") @Argument int amount) {
        return service.getLanguages(page-1, amount);
    }

    @QueryMapping
    @Secured("MODERATOR")
    public CourseList getCoursesForLanguage(@NotNull(message = "Language id must be provided!") @Argument Long languageId,
                                            @Min(value = 1, message = "Page must be positive") @NotNull(message = "Page cannot be null") @Argument int page,
                                            @NotNull @Min(value = 1, message = "Amount must be between 1 and 20") @Max(value = 20, message = "Amount must be between 1 and 20") @Argument int amount) {
        return service.getCourses(languageId, page-1, amount);
    }

    private LanguageRequest toRequest(String name) {
        return new LanguageRequest(name);
    }
}
