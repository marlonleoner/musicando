package com.marlonleoner.musicando.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.marlonleoner.musicando.util.Constants;

@Configuration
public class RabbitConfiguration {

    /**
     * Exchange principal do tipo DIRECT.
     * Roteia mensagens para filas baseadas em routing keys exatas.
     */
    @Bean
    public DirectExchange createDirectExchange() {
        return new DirectExchange(Constants.EVENTS_EXCHANGER, true, false);
    }

    /**
     * Conversor de mensagens para JSON usando Jackson.
     */
    @Bean
    public Jackson2JsonMessageConverter createJsonConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * Template de envio de mensagens, usando conversor JSON.
     */
    @Bean
    public RabbitTemplate createRabbitTemplate(CachingConnectionFactory factory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);
        rabbitTemplate.setMessageConverter(createJsonConverter());

        return rabbitTemplate;
    }

    /**
     * Admin do Rabbit para declarar filas dinamicamente em tempo de execução.
     */
    @Bean
    public AmqpAdmin createAmqpAdmin(CachingConnectionFactory factory) {
        return new RabbitAdmin(factory);
    }
}
