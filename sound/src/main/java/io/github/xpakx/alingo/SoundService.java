package io.github.xpakx.alingo;

import io.github.xpakx.alingo.dto.UploadResponse;
import io.github.xpakx.alingo.error.FileException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class SoundService {
    private final Path root = Paths.get("sounds");

    public Resource getSound(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public UploadResponse uploadSound(MultipartFile[] files) {
        try {
            List<String> fileNames = new ArrayList<>();
            Arrays.stream(files).forEach(file -> trySave(fileNames, file));
            UploadResponse response = new UploadResponse();
            response.setFiles(fileNames);
            return response;
        } catch (Exception e) {
            throw new FileException("Fail to upload files!");
        }
    }

    private void trySave(List<String> fileNames, MultipartFile file) {
        String name = Optional.ofNullable(file.getOriginalFilename())
                .orElseThrow(() -> new FileException("Filename cannot be empty!"));
        try {
            Files.copy(file.getInputStream(), this.root.resolve(name));
        } catch (Exception e) {
            throw new FileException("Could not store the file");
        }
        fileNames.add(file.getOriginalFilename());
    }
}
