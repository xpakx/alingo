package io.github.xpakx.alingo.settings;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AMQPConfig {
    private final String accountsTopic;

    public AMQPConfig(@Value("${amqp.exchange.accounts}") final String accountsTopic) {
        this.accountsTopic = accountsTopic;
    }

    @Bean
    public TopicExchange accountsTopicExchange() {
        return ExchangeBuilder
                .topicExchange(accountsTopic)
                .durable(true)
                .build();
    }

    @Bean
    public Jackson2JsonMessageConverter producerJackson2MessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}