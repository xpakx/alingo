package io.github.xpakx.alingo.game;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private Integer order;

    @ManyToOne
    @JsonIgnore
    private Course course;

}
