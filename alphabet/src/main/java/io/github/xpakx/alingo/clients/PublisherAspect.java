package io.github.xpakx.alingo.clients;

import io.github.xpakx.alingo.game.dto.AnswerResponse;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@AllArgsConstructor
public class PublisherAspect {
    private final GuessPublisher publisher;

    @Around(value="@annotation(PublishGuess) && args(exerciseId, ..)", argNames = "exerciseId")
    public AnswerResponse publishGuess(ProceedingJoinPoint joinPoint, Long exerciseId) throws Throwable {
        AnswerResponse result = (AnswerResponse) joinPoint.proceed();
        publisher.sendGuess(result, exerciseId, SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());
        return result;
    }
}
