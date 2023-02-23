package io.github.xpakx.alingo.game.dto;

import jakarta.validation.constraints.NotNull;

public record OrderRequest(@NotNull Integer newOrder) {
}
