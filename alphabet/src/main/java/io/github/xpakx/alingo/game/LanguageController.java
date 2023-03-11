package io.github.xpakx.alingo.game;

import io.github.xpakx.alingo.game.dto.CourseData;
import io.github.xpakx.alingo.game.dto.LanguageRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/language")
public class LanguageController {
    private final LanguageService service;

    @PostMapping
    @Secured("MODERATOR")
    public ResponseEntity<Language> createLanguage(@Valid @RequestBody LanguageRequest request) {
        return new ResponseEntity<>(
                service.createLanguage(request),
                HttpStatus.CREATED
        );
    }

    @PutMapping("/{languageId}")
    @Secured("MODERATOR")
    @ResponseBody
    public Language editLanguage(@Valid @RequestBody LanguageRequest request, @PathVariable Long languageId) {
        return service.editLanguage(languageId, request);
    }

    @GetMapping("/{languageId}")
    @Secured("MODERATOR")
    @ResponseBody
    public Language getLanguage(@PathVariable Long languageId) {
        return service.getLanguage(languageId);
    }

    @GetMapping("/{languageId}/course")
    @Secured("MODERATOR")
    @ResponseBody
    public List<CourseData> getCourses(@RequestParam  @Min(value = 1, message = "Page must be positive") @NotNull(message = "Page cannot be null") Integer page,
                                       @RequestParam @NotNull @Min(value = 1, message = "Amount must be between 1 and 20") @Max(value = 20, message = "Amount must be between 1 and 20") Integer amount,
                                       @PathVariable Long languageId) {
        return service.getCourses(languageId, page-1, amount);
    }

    @GetMapping
    @Secured("MODERATOR")
    @ResponseBody
    public List<Language> getLanguages(@RequestParam @Min(value = 1, message = "Page must be positive") @NotNull(message = "Page cannot be null") Integer page,
                                       @RequestParam @NotNull @Min(value = 1, message = "Amount must be between 1 and 20") @Max(value = 20, message = "Amount must be between 1 and 20") Integer amount) {
        return service.getLanguages(page-1, amount);
    }

    @GetMapping("/byName/{name}")
    @Secured("MODERATOR")
    @ResponseBody
    public List<Language> getLanguagesByName(@PathVariable @NotBlank String name,
                                             @RequestParam @Min(value = 1, message = "Page must be positive") @NotNull(message = "Page cannot be null") Integer page,
                                             @RequestParam @NotNull @Min(value = 1, message = "Amount must be between 1 and 20") @Max(value = 20, message = "Amount must be between 1 and 20") Integer amount) {
        return service.findLanguages(name, page-1, amount);
    }
}
