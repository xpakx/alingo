package io.github.xpakx.alingo.sound;

import io.github.xpakx.alingo.sound.dto.FilesResponse;
import io.github.xpakx.alingo.sound.dto.UploadResponse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
@Validated
public class SoundController {
    private final SoundService service;

    @GetMapping("/sound/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = service.getSound(filename);
        return ResponseEntity.ok().header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\""
        ).body(file);
    }

    @PostMapping("/sound")
    @ResponseBody
    @Secured("MODERATOR")
    public UploadResponse uploadFiles(@RequestParam("files") MultipartFile[] files) {
        return service.uploadSound(files);
    }

    @GetMapping("/sound/list")
    @Secured("MODERATOR")
    @ResponseBody
    public FilesResponse getListOfFiles(@Min(value = 1, message = "Page must be positive") @NotNull(message = "Page cannot be null") @RequestParam Integer page) {
        return service.getFileNames(page-1);
    }
}
