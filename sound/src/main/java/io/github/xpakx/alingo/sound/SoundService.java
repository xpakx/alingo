package io.github.xpakx.alingo.sound;

import io.github.xpakx.alingo.sound.dto.FileError;
import io.github.xpakx.alingo.sound.dto.FilesResponse;
import io.github.xpakx.alingo.sound.dto.UploadResponse;
import io.github.xpakx.alingo.sound.error.FileException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SoundService {
    private final SoundRepository repository;
    private final Path root;
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
        repository.saveAll(fileNames.stream().map(this::toSoundEntity).toList());
        return response;
    }

    private Sound toSoundEntity(String name) {
        Sound sound = new Sound();
        sound.setFilename(name);
        return sound;
    }

    private void trySave(List<String> fileNames, List<FileError> fileErrors, MultipartFile file) {
        if(file.getOriginalFilename() == null || file.getOriginalFilename().equals("")) {
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

    public FilesResponse getFileNames(Integer page) {
        FilesResponse response = new FilesResponse();
        response.setFiles(getFilenamesFromDb(page));
        return response;
    }

    private List<String> getFilenamesFromDb(Integer page) {
        return repository.findAll(createPageRequestSortedById(page))
                .map(Sound::getFilename)
                .toList();
    }

    private static PageRequest createPageRequestSortedById(Integer page) {
        return PageRequest.of(
                page,
                20,
                Sort.by(Sort.Order.asc("filename"))
        );
    }
}
