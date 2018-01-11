/*
 * Copyright 2017 Leibniz Institut für Analytische Wissenschaften - ISAS e.V..
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.isas.lipidomics.mztab.validator.webapp.service.storage;

import de.isas.lipidomics.mztab.validator.webapp.domain.UserSessionFile;
import de.isas.lipidomics.mztab.validator.webapp.service.StorageService;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author Nils Hoffmann <nils.hoffmann@isas.de>
 */
@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public UserSessionFile store(MultipartFile file, String sessionId) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            if (file.isEmpty()) {
                throw new StorageException(
                    "Failed to store empty file " + filename);
            }

            Path sessionPath = buildSessionPath(sessionId);
            Files.createDirectories(sessionPath);
            Files.copy(file.getInputStream(), buildPathToFile(sessionPath,
                filename),
                StandardCopyOption.REPLACE_EXISTING);
            return new UserSessionFile(filename, sessionId);
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public UserSessionFile store(String fileContent, String sessionId) {
        String filename = UUID.randomUUID() + ".mztab";
        try {
            Path sessionPath = buildSessionPath(sessionId);
            Files.createDirectories(sessionPath);
            Files.write(buildPathToFile(sessionPath, filename), fileContent.
                getBytes("UTF-8"), StandardOpenOption.CREATE,
                StandardOpenOption.WRITE);
            return new UserSessionFile(filename, sessionId);
        } catch (IOException e) {
            throw new StorageException("Failed to store file " + filename, e);
        }
    }

    @Override
    public Stream<Path> loadAll(String sessionId) {
        Path sessionPath = buildSessionPath(sessionId);
        try {
            return Files.walk(sessionPath, 1).
                filter(path ->
                    !path.equals(sessionPath)).
                map(path ->
                    sessionPath.relativize(path));
        } catch (IOException e) {
            throw new StorageException("Failed to read stored files", e);
        }

    }

    private Path buildSessionPath(String sessionId) {
        if (sessionId == null) {
            throw new StorageException(
                "Cannot store file when sessionId is null!");
        }
        String sessionPathId = UUID.nameUUIDFromBytes(sessionId.getBytes()).
            toString();
        return this.rootLocation.resolve(sessionPathId);
    }

    private Path buildPathToFile(Path sessionPath, String filename) {
        if (filename.contains("..")) {
            // This is a security check
            throw new StorageException(
                "Cannot store file with relative path outside current directory "
                + filename);
        }
        return sessionPath.resolve(filename);
    }

    @Override
    public Path load(UserSessionFile userSessionFile) {
        Path p = buildSessionPath(userSessionFile.getSessionId());
        return p.resolve(userSessionFile.getFilename());
    }

    @Override
    public Resource loadAsResource(UserSessionFile userSessionFile) {
        if (userSessionFile == null) {
            throw new StorageException(
                "Cannot retrieve file when userSessionFile is null!");
        }
        try {
            Path file = load(userSessionFile);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException(
                    "Could not read file: " + userSessionFile.getFilename());

            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException(
                "Could not read file: " + userSessionFile.getFilename(), e);
        }
    }

    @Override
    public void deleteAll(String sessionId) {
        Path sessionPath = buildSessionPath(sessionId);
        FileSystemUtils.deleteRecursively(sessionPath.toFile());
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(this.rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
