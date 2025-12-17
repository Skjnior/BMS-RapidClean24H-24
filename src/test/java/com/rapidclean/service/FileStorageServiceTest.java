package com.rapidclean.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        // Utiliser un répertoire temporaire pour les tests
        try {
            java.lang.reflect.Field field = FileStorageService.class.getDeclaredField("uploadDir");
            field.setAccessible(true);
            field.set(fileStorageService, tempDir.toString());
        } catch (Exception e) {
            fail("Failed to set upload directory");
        }
    }

    @Test
    void testStoreFile_ValidImage() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        // When
        String filename = fileStorageService.storeFile(file);

        // Then
        assertNotNull(filename);
        assertTrue(filename.endsWith(".jpg"));
    }

    @Test
    void testStoreFile_InvalidFileType() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.pdf",
            "application/pdf",
            "test content".getBytes()
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file);
        });
    }

    @Test
    void testStoreFile_FileTooLarge() {
        // Given
        byte[] largeContent = new byte[6 * 1024 * 1024]; // 6 MB
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "large.jpg",
            "image/jpeg",
            largeContent
        );

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.storeFile(file);
        });
    }

    @Test
    void testStoreFile_NullFile() throws IOException {
        // When
        String filename = fileStorageService.storeFile(null);

        // Then
        assertNull(filename);
    }

    @Test
    void testStoreFile_EmptyFile() throws IOException {
        // Given
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.jpg",
            "image/jpeg",
            new byte[0]
        );

        // When
        String filename = fileStorageService.storeFile(file);

        // Then
        assertNull(filename);
    }
}



