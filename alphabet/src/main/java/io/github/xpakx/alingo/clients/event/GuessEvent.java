package io.github.xpakx.alingo.clients.event;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuessEvent {
    private String username;
    private Long exerciseId;
    private boolean correct;
}
