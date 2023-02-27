package io.github.xpakx.alingo.settings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.annotation.support.MessageHandlerMethodFactory;

@Configuration
public class AMQPConfig {

    @Bean
    public TopicExchange guessesTopicExchange(@Value("${amqp.exchange.guesses}") final String exchangeName) {
        return ExchangeBuilder.topicExchange(exchangeName).durable(true).build();
    }

    @Bean
    public Queue statsQueue(@Value("${amqp.queue.stats}") final String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    @Bean
    public Binding guessesBinding(final Queue gamificationQueue, final TopicExchange guessesExchange) {
        return BindingBuilder.bind(gamificationQueue)
                .to(guessesExchange)
                .with("guess");
    }

    @Bean
    public MessageHandlerMethodFactory messageHandlerMethodFactory() {
        DefaultMessageHandlerMethodFactory factory = new DefaultMessageHandlerMethodFactory();
        final MappingJackson2MessageConverter jsonConverter = new MappingJackson2MessageConverter();
        jsonConverter.getObjectMapper().registerModule(
                new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        factory.setMessageConverter(jsonConverter);
        return factory;
    }

    @Bean
    public RabbitListenerConfigurer rabbitListenerConfigurer(
            final MessageHandlerMethodFactory messageHandlerMethodFactory) {
        return (c) -> c.setMessageHandlerMethodFactory(messageHandlerMethodFactory);
    }
}
