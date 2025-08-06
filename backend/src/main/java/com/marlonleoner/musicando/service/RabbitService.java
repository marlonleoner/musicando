package com.marlonleoner.musicando.service;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.marlonleoner.musicando.domain.event.Event;
import com.marlonleoner.musicando.util.Constants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Service
public class RabbitService {

    private final AmqpAdmin amqpAdmin;

    private final DirectExchange exchange;

    private final RabbitTemplate template;

    public void declareQueue(String code) {
        String queueName = String.format(Constants.EVENTS_ROOM_QUEUE, code);

        Queue queue = new Queue(queueName, true);
        Binding binding = BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(queueName);

        amqpAdmin.declareQueue(queue);
        amqpAdmin.declareBinding(binding);

        log.info("Queue and binding declared for room '{}'", code);
    }

    public void sendMessage(String queue, Object object) {
        template.convertAndSend(queue, object);
    }

    public void sendEvent(Event event) {
        String queueName = String.format(Constants.EVENTS_ROOM_QUEUE, event.getCode());

        template.convertAndSend(Constants.EVENTS_EXCHANGER, queueName, event);
    }
}