package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.ExercisesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

class ExerciseServiceImplTest {
    Random random = new Random(2109402385802350823L);
    ExerciseService service  = new ExerciseServiceImpl();

    @BeforeEach
    void setUp() {
        service = new ExerciseServiceImpl();
    }

    @Test
    public void shouldRandomizeOrderOfAnswers() {
        List<Exercise> exercises = new ArrayList<>();
        for(int i=0; i<5; i++) {
            Exercise exercise = new Exercise();
            exercise.setCorrectAnswer("correct");
            exercise.setWrongAnswer("wrong");
            exercises.add(exercise);
        }
        Page<Exercise> page = new PageImpl<>(exercises, Pageable.ofSize(5), 5);

        ExercisesResponse response = service.prepareResponse(page, random);
        assertThat(response.getExercises(), hasItem(hasProperty("options", contains("wrong", "correct"))));
        assertThat(response.getExercises(), hasItem(hasProperty("options", contains("correct", "wrong"))));
    }

}