package com.rapidclean.service;

import com.rapidclean.entity.AuditLog;
import com.rapidclean.entity.User;
import com.rapidclean.repository.AuditLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditService auditService;

    private User testUser;
    private AuditRequestInfo testRequestInfo;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testRequestInfo = new AuditRequestInfo(
            "GET",
            "/test/url",
            "127.0.0.1",
            "Mozilla/5.0",
            "session123",
            "http://referer.com",
            "fr-FR",
            "param=value"
        );
    }

    @Test
    void testLogActivityWithUser() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        // When
        auditService.logActivity(
            testUser,
            testRequestInfo,
            "TEST_ACTION",
            "TEST_TYPE",
            "TestResource",
            123L,
            200,
            150L,
            null
        );

        // Then
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testLogActivityWithoutUser() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenAnswer(invocation -> {
            AuditLog log = invocation.getArgument(0);
            log.setId(1L);
            return log;
        });

        // When
        auditService.logActivity(
            testRequestInfo,
            "TEST_ACTION",
            "TEST_TYPE",
            "TestResource",
            123L,
            200,
            150L,
            null
        );

        // Then
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testGetAuditLogs() {
        // Given
        List<AuditLog> logs = new ArrayList<>();
        AuditLog log = new AuditLog();
        log.setId(1L);
        log.setAction("TEST_ACTION");
        logs.add(log);

        Page<AuditLog> page = new PageImpl<>(logs);
        Pageable pageable = PageRequest.of(0, 10);

        when(auditLogRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        // When
        Page<AuditLog> result = auditService.getAuditLogs(
            null, null, null, null, null, null, null, null, pageable
        );

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(auditLogRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    void testCleanOldLogs() {
        // Given
        int daysToKeep = 30;

        // When
        auditService.cleanOldLogs(daysToKeep);

        // Then
        verify(auditLogRepository, times(1)).deleteByTimestampBefore(any(LocalDateTime.class));
    }

    @Test
    void testCleanLogsByPeriod() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        // When
        auditService.cleanLogsByPeriod(startDate, endDate);

        // Then
        verify(auditLogRepository, times(1)).deleteByTimestampBetween(eq(startDate), any(LocalDateTime.class));
    }

    @Test
    void testCleanLogsByPeriodWithNullDates() {
        // Given
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            auditService.cleanLogsByPeriod(startDate, endDate);
        });
    }
}



