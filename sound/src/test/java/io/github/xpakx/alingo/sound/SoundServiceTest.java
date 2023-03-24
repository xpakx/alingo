package io.github.xpakx.alingo.sound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SoundServiceTest {
    @TempDir
    public File tempDir;
    SoundService service;

    @BeforeEach
    void setUp() {
        service = new SoundService(Mockito.mock(SoundRepository.class), tempDir.toPath());
    }

    @Test
    void shouldSaveFile() {
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "test.mp3",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "test content".getBytes()
        );
        service.uploadSound(new MultipartFile[] {file});

        assertTrue(Files.exists(tempDir.toPath().resolve("test.mp3")));
    }

    @Test
    void shouldNotSaveFileWithNullName() {
        MockMultipartFile file = new MockMultipartFile(
                "files",
                null,
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "test content".getBytes()
        );
        service.uploadSound(new MultipartFile[] {file});

        assertTrue(Files.notExists(tempDir.toPath().resolve("test.mp3")));
    }

    @Test
    void shouldNotSaveFileWithEmptyName() {
        MockMultipartFile file = new MockMultipartFile(
                "files",
                "",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "test content".getBytes()
        );
        service.uploadSound(new MultipartFile[] {file});

        assertTrue(Files.notExists(tempDir.toPath().resolve("test.mp3")));
    }
}