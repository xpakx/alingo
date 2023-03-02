package io.github.xpakx.alingo.utils;

import lombok.Data;

@Data
public class GraphQuery {
    private String query;
    private Object variables;
}
