package io.github.xpakx.alingo.stats;

import io.github.xpakx.alingo.stats.event.GuessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatService {
    private final GuessRepository repository;

    public void newGuess(GuessEvent event) {
        Guess guess = new Guess();
        guess.setCorrect(event.isCorrect());
        guess.setCourseId(event.getCourseId());
        guess.setCourseName(event.getCourseName());
        guess.setExerciseId(event.getExerciseId());
        guess.setLanguage(event.getLanguage());
        guess.setLetter(event.getLetter());
        guess.setUsername(event.getUsername());
        repository.save(guess);
    }
}
