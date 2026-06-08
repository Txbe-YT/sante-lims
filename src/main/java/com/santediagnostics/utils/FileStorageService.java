package com.santediagnostics.utils;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author dasil
 */

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileStorageService {
    // Saves files to a "sante_lims_uploads" folder in the user's home directory
    private static final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "sante_lims_uploads" + File.separator;

    public FileStorageService() {
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    /**
     * Copies the selected file to the secure upload directory.
     * @return The absolute path to the saved file (to be stored in PostgreSQL).
     */
    public String saveFile(File selectedFile) throws IOException {
        if (selectedFile == null || !selectedFile.exists()) {
            return null;
        }

        // Keep the original extension
        String extension = "";
        String fileName = selectedFile.getName();
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i);
        }

        // Generate a random UUID name to prevent file overwrite collisions
        String uniqueFileName = UUID.randomUUID().toString() + extension;
        Path destination = Paths.get(UPLOAD_DIR + uniqueFileName);

        Files.copy(selectedFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

        return destination.toString(); 
    }

    /**
     * Retrieves a file given its absolute path.
     */
    public File getFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return null;
        }
        File file = new File(filePath);
        return file.exists() ? file : null;
    }
}
