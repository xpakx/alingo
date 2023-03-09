package io.github.xpakx.alingo.game.dto;

public interface ExerciseData {
    Long getId();
    String getLetter();
    String getWrongAnswer();
    String getCorrectAnswer();
    CourseMin getCourse();
}
