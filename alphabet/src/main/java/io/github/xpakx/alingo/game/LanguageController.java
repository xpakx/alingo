package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.AnswerRequest;
import io.github.xpakx.alingo.game.dto.AnswerResponse;
import io.github.xpakx.alingo.game.dto.LanguageRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/language")
public class LanguageController {
    private final LanguageService service;

    @PostMapping
    public ResponseEntity<Language> createLanguage(@Valid @RequestBody LanguageRequest request) {
        return new ResponseEntity<>(
                service.createLanguage(request),
                HttpStatus.OK
        );
    }

    @PutMapping("/{languageId}")
    public ResponseEntity<Language> editLanguage(@Valid @RequestBody LanguageRequest request, @PathVariable Long languageId) {
        return new ResponseEntity<>(
                service.editLanguage(languageId, request),
                HttpStatus.OK
        );
    }
}
