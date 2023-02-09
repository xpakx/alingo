package io.github.xpakx.alingo.game.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerResponse {
    boolean correct;
    String correctAnswer;
}
