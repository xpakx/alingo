package io.github.xpakx.alingo.stats;

import io.github.xpakx.alingo.stats.event.GuessEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuessEventHandler {
    private final StatService statService;

    @RabbitListener(queues = "${amqp.queue.stats}")
    void handleGuess(final GuessEvent event) {
        try {
            statService.newGuess(event);
        } catch (final Exception e) {
            throw new AmqpRejectAndDontRequeueException(e);
        }
    }
}