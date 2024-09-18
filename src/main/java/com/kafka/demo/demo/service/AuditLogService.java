package com.kafka.demo.demo.service;


import com.kafka.demo.demo.entity.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.kafka.demo.demo.repository.AuditLogRepository;

@Service
@RequiredArgsConstructor
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    @KafkaListener(topics = "product-events", groupId = "spring-group")
    public void listen(String message) {
        AuditLog auditLog = new AuditLog();
        auditLog.setEntityName("Product");
        auditLog.setMessage(message);
        auditLogRepository.save(auditLog);
    }
}

