package io.github.xpakx.alingo.game.graphql;

import io.github.xpakx.alingo.game.dto.AnswerResponse;
import io.github.xpakx.alingo.game.dto.ExercisesResponse;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class GraphGameController {
    @QueryMapping
    public ExercisesResponse courseExercises(@Argument long course, @Argument int page, @Argument int amount) {
        return null;
    }

    @MutationMapping
    public AnswerResponse answer(@Argument long exercise, @Argument String guess) {
        return null;
    }
}
