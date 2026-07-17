package com.tracelens.evidence.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.tracelens.evidence.config.EvidenceProperties;
import com.tracelens.evidence.entity.EvidenceFileType;
import com.tracelens.exception.EvidenceStorageException;

import jakarta.annotation.PostConstruct;

@Service
public class EvidenceStorageService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(
                    EvidenceStorageService.class
            );

    private static final int STORAGE_ATTEMPTS = 5;

    private static final int HASH_BUFFER_SIZE = 8192;

    private static final String SHA_256_ALGORITHM =
            "SHA-256";

    private final Path storageRoot;

    public EvidenceStorageService(
            EvidenceProperties evidenceProperties
    ) {

        this.storageRoot = Paths
                .get(evidenceProperties.getStorageRoot())
                .toAbsolutePath()
                .normalize();
    }

    @PostConstruct
    public void initializeStorage() {

        try {
            Files.createDirectories(storageRoot);

            LOGGER.info(
                    "Evidence storage initialized at {}",
                    storageRoot
            );
        }
        catch (IOException exception) {
            throw new EvidenceStorageException(
                    "Unable to initialize evidence storage",
                    exception
            );
        }
    }

    public StoredEvidenceFile store(
            Long caseId,
            MultipartFile file,
            EvidenceFileType fileType
    ) {

        if (caseId == null || caseId <= 0) {
            throw new EvidenceStorageException(
                    "Cannot store evidence for an invalid case"
            );
        }

        Path caseDirectory =
                resolveCaseDirectory(caseId);

        try {
            Files.createDirectories(caseDirectory);
        }
        catch (IOException exception) {
            throw new EvidenceStorageException(
                    "Unable to create case evidence directory",
                    exception
            );
        }

        String extension = fileType
                .name()
                .toLowerCase(Locale.ROOT);

        for (
                int attempt = 0;
                attempt < STORAGE_ATTEMPTS;
                attempt++
        ) {

            String storedFileName =
                    UUID.randomUUID()
                    + "."
                    + extension;

            Path targetPath = caseDirectory
                    .resolve(storedFileName)
                    .normalize();

            verifyPathInsideStorage(
                    targetPath,
                    caseDirectory
            );

            try (
                    InputStream inputStream =
                            file.getInputStream()
            ) {

                long copiedBytes = Files.copy(
                        inputStream,
                        targetPath
                );

                long storedFileSize =
                        Files.size(targetPath);

                if (copiedBytes != storedFileSize
                        || storedFileSize
                        != file.getSize()) {

                    deletePathQuietly(targetPath);

                    throw new EvidenceStorageException(
                            "Stored evidence size does not "
                            + "match the uploaded file"
                    );
                }

                String sha256Hash =
                        calculateSha256(targetPath);

                String relativePath = storageRoot
                        .relativize(targetPath)
                        .toString()
                        .replace(
                                File.separatorChar,
                                '/'
                        );

                return new StoredEvidenceFile(
                        storedFileName,
                        relativePath,
                        storedFileSize,
                        sha256Hash
                );
            }
            catch (FileAlreadyExistsException exception) {
                // Generate another UUID and retry.
            }
            catch (IOException exception) {

                deletePathQuietly(targetPath);

                throw new EvidenceStorageException(
                        "Unable to store the evidence file",
                        exception
                );
            }
            catch (RuntimeException exception) {

                deletePathQuietly(targetPath);

                throw exception;
            }
        }

        throw new EvidenceStorageException(
                "Unable to generate a unique stored filename"
        );
    }

    public String calculateSha256(
            String relativePath
    ) {

        Path targetPath =
                resolveRelativePath(relativePath);

        verifyReadableFile(targetPath);

        return calculateSha256(targetPath);
    }

    public Resource loadAsResource(
            String relativePath
    ) {

        Path targetPath =
                resolveRelativePath(relativePath);

        verifyReadableFile(targetPath);

        try {
            Resource resource =
                    new UrlResource(targetPath.toUri());

            if (!resource.exists()
                    || !resource.isReadable()) {

                throw new EvidenceStorageException(
                        "The stored evidence file is unavailable"
                );
            }

            return resource;
        }
        catch (MalformedURLException exception) {
            throw new EvidenceStorageException(
                    "The stored evidence path is invalid",
                    exception
            );
        }
    }

    public void delete(
            String relativePath
    ) {

        Path targetPath =
                resolveRelativePath(relativePath);

        try {
            Files.deleteIfExists(targetPath);

            deleteEmptyParentDirectory(
                    targetPath.getParent()
            );
        }
        catch (IOException exception) {
            throw new EvidenceStorageException(
                    "Unable to delete the stored evidence file",
                    exception
            );
        }
    }

    public void deleteQuietly(
            String relativePath
    ) {

        try {
            delete(relativePath);
        }
        catch (RuntimeException exception) {

            LOGGER.warn(
                    "Unable to remove stored evidence file: {}",
                    relativePath,
                    exception
            );
        }
    }

    private String calculateSha256(
            Path targetPath
    ) {

        MessageDigest messageDigest =
                createSha256Digest();

        byte[] buffer =
                new byte[HASH_BUFFER_SIZE];

        try (
                InputStream inputStream =
                        Files.newInputStream(targetPath)
        ) {

            int bytesRead;

            while (
                    (bytesRead = inputStream.read(buffer))
                    != -1
            ) {

                messageDigest.update(
                        buffer,
                        0,
                        bytesRead
                );
            }

            byte[] digest =
                    messageDigest.digest();

            return HexFormat
                    .of()
                    .formatHex(digest);
        }
        catch (IOException exception) {
            throw new EvidenceStorageException(
                    "Unable to calculate evidence SHA-256 hash",
                    exception
            );
        }
    }

    private MessageDigest createSha256Digest() {

        try {
            return MessageDigest.getInstance(
                    SHA_256_ALGORITHM
            );
        }
        catch (NoSuchAlgorithmException exception) {
            throw new EvidenceStorageException(
                    "SHA-256 hashing is unavailable",
                    exception
            );
        }
    }

    private void verifyReadableFile(
            Path targetPath
    ) {

        if (!Files.exists(targetPath)
                || !Files.isRegularFile(targetPath)
                || !Files.isReadable(targetPath)) {

            throw new EvidenceStorageException(
                    "The stored evidence file is unavailable"
            );
        }
    }

    private Path resolveCaseDirectory(
            Long caseId
    ) {

        Path caseDirectory = storageRoot
                .resolve("case-" + caseId)
                .normalize();

        verifyPathInsideStorage(
                caseDirectory,
                storageRoot
        );

        return caseDirectory;
    }

    private Path resolveRelativePath(
            String relativePath
    ) {

        if (relativePath == null
                || relativePath.isBlank()) {

            throw new EvidenceStorageException(
                    "Stored evidence path is missing"
            );
        }

        Path resolvedPath = storageRoot
                .resolve(relativePath)
                .normalize();

        verifyPathInsideStorage(
                resolvedPath,
                storageRoot
        );

        return resolvedPath;
    }

    private void verifyPathInsideStorage(
            Path candidatePath,
            Path expectedParent
    ) {

        if (!candidatePath.startsWith(expectedParent)) {
            throw new EvidenceStorageException(
                    "Evidence storage path is invalid"
            );
        }
    }

    private void deleteEmptyParentDirectory(
            Path directory
    ) {

        if (directory == null
                || directory.equals(storageRoot)
                || !directory.startsWith(storageRoot)) {

            return;
        }

        try (
                var children = Files.list(directory)
        ) {

            if (children.findAny().isEmpty()) {
                Files.deleteIfExists(directory);
            }
        }
        catch (IOException exception) {

            LOGGER.warn(
                    "Unable to remove empty evidence directory: {}",
                    directory,
                    exception
            );
        }
    }

    private void deletePathQuietly(
            Path path
    ) {

        try {
            Files.deleteIfExists(path);
        }
        catch (IOException exception) {

            LOGGER.warn(
                    "Unable to remove incomplete evidence file: {}",
                    path,
                    exception
            );
        }
    }
}