package io.github.xpakx.alingo;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
}
