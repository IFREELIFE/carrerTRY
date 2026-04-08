package com.ifreelife.carrertry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskDispatchService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.ai.queue-name:careertry.ai.tasks}")
    private String queueName;

    public boolean dispatchRetry(Long taskId, String taskName, Integer retryCount, LocalDateTime queuedAt) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("taskId", taskId);
        payload.put("taskName", taskName);
        payload.put("taskStatus", "QUEUED");
        payload.put("retryCount", retryCount);
        payload.put("updatedAt", DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(queuedAt));
        try {
            rabbitTemplate.convertAndSend(queueName, payload);
            return true;
        } catch (AmqpException ex) {
            log.warn("RabbitMQ dispatch failed for task {}: {}", taskId, ex.getMessage());
            return false;
        }
    }
}
