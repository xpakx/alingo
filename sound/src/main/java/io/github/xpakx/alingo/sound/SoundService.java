package io.github.xpakx.alingo.sound;

import io.github.xpakx.alingo.sound.dto.FileError;
import io.github.xpakx.alingo.sound.dto.UploadResponse;
import io.github.xpakx.alingo.sound.error.FileException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    Logger logger = LoggerFactory.getLogger(SoundService.class);

    @PostConstruct
    public void init() {
        try {
            if(!Files.exists(root)) {
                Files.createDirectory(root);
            } else if(!Files.isDirectory(root)) {
                logger.error("Cannot create directory for file upload!");
            }
        } catch (IOException e) {
            logger.error("Cannot create directory for file upload!");
        }
    }

    public Resource getSound(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new FileException(e.getMessage());
        }
    }

    public UploadResponse uploadSound(MultipartFile[] files) {
        List<String> fileNames = new ArrayList<>();
        List<FileError> fileErrors = new ArrayList<>();
        Arrays.stream(files).forEach(file -> trySave(fileNames, fileErrors, file));
        UploadResponse response = new UploadResponse();
        response.setFiles(fileNames);
        response.setErrors(fileErrors);
        return response;
    }

    private void trySave(List<String> fileNames, List<FileError> fileErrors, MultipartFile file) {
        if(file.getOriginalFilename() == null) {
            fileErrors.add(new FileError("","Filename cannot be empty!" ));
            return;
        }
        String name = file.getOriginalFilename();
        try {
            Files.copy(file.getInputStream(), this.root.resolve(name));
        } catch (Exception e) {
            fileErrors.add(new FileError(name,"Could not store the file"));
            return;
        }
        fileNames.add(file.getOriginalFilename());
    }
}
