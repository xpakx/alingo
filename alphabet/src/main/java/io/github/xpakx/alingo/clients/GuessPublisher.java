package io.github.xpakx.alingo.clients;

import io.github.xpakx.alingo.clients.event.GuessEvent;
import io.github.xpakx.alingo.game.dto.AnswerResponse;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class GuessPublisher {
    private final AmqpTemplate template;
    private final String guessesTopic;

    public GuessPublisher(AmqpTemplate template, @Value("${amqp.exchange.guesses}") String guessesTopic) {
        this.template = template;
        this.guessesTopic = guessesTopic;
    }

    public void sendGuess(AnswerResponse answer, Long exerciseId, String username) {
        GuessEvent event = new GuessEvent();
        event.setCorrect(answer.correct());
        event.setUsername(username);
        event.setExerciseId(exerciseId);
        event.setLetter(answer.letter());
        event.setCourseId(answer.courseId());
        event.setCourseName(answer.courseName());
        event.setLanguage(answer.language());
        event.setTime(LocalDateTime.now());
        template.convertAndSend(guessesTopic, "guess", event);
    }
}