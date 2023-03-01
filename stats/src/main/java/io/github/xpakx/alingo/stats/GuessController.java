package io.github.xpakx.alingo.stats;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class GuessController {
    private final GuessService service;

    @GetMapping("/stats/{username}/alphabet")
    @ResponseBody
    @PreAuthorize("#username == authentication.principal.username")
    public Page<Guess> getGuesses(@RequestParam Integer page,
                                  @RequestParam Integer amount,
                                  @PathVariable String username) {
        return service.getGuesses(username, page-1, amount);
    }

    /*
    (@RequestParam @Min(value = 1, message = "Page must be positive") @NotNull(message = "Page cannot be null") Integer page,
                                                          @RequestParam @NotNull @Min(value = 1, message = "Amount must be between 1 and 20") @Max(value = 20, message = "Amount must be between 1 and 20") Integer amount,
                                                          @PathVariable Long courseId) {
     */
}
