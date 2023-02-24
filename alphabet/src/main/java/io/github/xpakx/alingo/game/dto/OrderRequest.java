package io.github.xpakx.alingo.game.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record OrderRequest(@NotNull(message = "Order must be provided") @PositiveOrZero(message = "Order cannot be negative") Integer newOrder) {
}
