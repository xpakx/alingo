package io.github.xpakx.alingo.stats;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GuessService {
    private final GuessRepository repository;

    public Page<Guess> getGuesses(String username, Integer page, Integer amount) {
        return  repository.findByUsername(username, toPageRequest(page, amount));
    }

    private PageRequest toPageRequest(Integer page, Integer amount) {
        return PageRequest.of(page, amount, Sort.by(Sort.Order.desc("id")));
    }
}
