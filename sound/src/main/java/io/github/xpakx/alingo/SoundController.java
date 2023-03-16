package io.github.xpakx.alingo;

import io.github.xpakx.alingo.dto.UploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class SoundController {
    private final SoundService service;

    @GetMapping("/sound/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        System.out.println(filename);
        Resource file = service.getSound(filename);
        return ResponseEntity.ok().header(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\""
        ).body(file);
    }

    @PostMapping("/sound")
    @ResponseBody
    public UploadResponse uploadFiles(@RequestParam("files") MultipartFile[] files) {
        return service.uploadSound(files);
    }
}
