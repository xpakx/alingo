package io.github.xpakx.alingo.stats.graphql;

import io.github.xpakx.alingo.stats.Guess;
import io.github.xpakx.alingo.stats.GuessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class GraphGuessController {
    private final GuessService service;

    @QueryMapping
    public Page<Guess> getGuesses(@Argument String username, @Argument int page, @Argument int amount) {
        return service.getGuesses(username, page-1, amount);
    }
}
