package io.github.xpakx.alingo.settings;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class FileConfig {
    @Bean
    public Path rootPath(@Value("${filename.root}") String filename) {
        return Paths.get(filename);
    }
}
