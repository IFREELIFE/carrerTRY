package com.ifreelife.carrertry.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitTaskConfig {

    @Bean
    public Queue aiTaskQueue(@Value("${app.ai.queue-name:careertry.ai.tasks}") String queueName) {
        return QueueBuilder.durable(queueName).build();
    }
}
