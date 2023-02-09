package io.github.xpakx.alingo.game;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Exercise {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String letter;

    @Column(nullable = false)
    private String wrongAnswer;

    @Column(nullable = false)
    private String correctAnswer;

    @ManyToOne
    private Course course;

}
