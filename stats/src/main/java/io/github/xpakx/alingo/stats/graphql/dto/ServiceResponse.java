package io.github.xpakx.alingo.stats.graphql.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
public class ServiceResponse {
    private String name;
    private String version;
    private String schema;
}
