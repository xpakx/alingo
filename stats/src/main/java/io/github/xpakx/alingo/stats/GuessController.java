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
    public Page<Guess> getGuesses(Integer page, Integer amount, @PathVariable String username) {
        return service.getGuesses(username, page-1, amount);
    }
}
