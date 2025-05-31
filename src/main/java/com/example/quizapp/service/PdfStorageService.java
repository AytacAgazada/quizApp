package com.example.quizapp.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class PdfStorageService {

    private final Path storagePath = Paths.get("pdf-storage").toAbsolutePath().normalize();

    public PdfStorageService() throws IOException {
        Files.createDirectories(storagePath);
    }

    public String saveFile(MultipartFile file) throws IOException {
        String filename = "uploaded.pdf"; // UUID ilə dəyişə bilərsən, amma bura sabit ad qoymuşuq
        Path target = storagePath.resolve(filename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return target.toString();
    }

    public Path getStoredFile() {
        return storagePath.resolve("uploaded.pdf");
    }

    public boolean isFileStored() {
        return Files.exists(getStoredFile());
    }
}
