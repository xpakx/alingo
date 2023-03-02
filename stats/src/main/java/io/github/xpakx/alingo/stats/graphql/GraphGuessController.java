package io.github.xpakx.alingo.stats.graphql;

import io.github.xpakx.alingo.stats.Guess;
import io.github.xpakx.alingo.stats.GuessService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@RequiredArgsConstructor
@Validated
public class GraphGuessController {
    private final GuessService service;

    @QueryMapping
    public Page<Guess> getGuesses(@NotBlank @Argument String username,
                                  @Min(value = 1) @Argument int page,
                                  @NotNull @Min(value = 1) @Max(value = 20) @Argument int amount) {
        return service.getGuesses(username, page-1, amount);
    }
}
