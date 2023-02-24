package io.github.xpakx.alingo.settings;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AMQPConfig {
    private final String guessesTopic;

    public AMQPConfig(@Value("${amqp.exchange.guesses}") final String guessesTopic) {
        this.guessesTopic = guessesTopic;
    }

    @Bean
    public TopicExchange guessesTopicExchange() {
        return ExchangeBuilder
                .topicExchange(guessesTopic)
                .durable(true)
                .build();
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}