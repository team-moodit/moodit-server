package com.team.moodit.support.file;

import com.team.moodit.domain.enums.ObjectResourceType;
import com.team.moodit.storage.db.core.FileEntity;
import com.team.moodit.storage.db.core.FileRepository;
import com.team.moodit.storage.db.s3.S3Uploader;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploader {
    private final S3Uploader s3Uploader;
    private final FileRepository fileRepository;
    private final ObjectKeyGenerator objectKeyGenerator;

    @Value("${storage.s3.endpoint}") private String endpoint;
    @Value("${storage.s3.bucket}") private String bucket;

    public UploadResult upload(Long userId, ObjectResourceType resourceType, MultipartFile file) {
        try {
            String extension = extractExtension(file);
            String objectKey = objectKeyGenerator.generate(resourceType, extension);

            s3Uploader.uploadFile(file, objectKey);

            FileEntity savedFile = fileRepository.save(
                    new FileEntity(
                            userId,
                            resourceType,
                            objectKey,
                            file.getOriginalFilename(),
                            file.getContentType(),
                            file.getSize()
                    )
            );

            return new UploadResult(
                    savedFile.getId(),
                    endpoint + "/" + bucket + "/" + objectKey
            );
        } catch (Exception e) {
            log.error("[FileUploader] userId: {}, filename: {}. size: {} bytes, message: {}", userId, file.getOriginalFilename(), file.getSize(), e.getMessage(), e);
            throw new ApiException(ErrorType.FILE_UPLOADING_FAILED);
        }
    }

    private String extractExtension(MultipartFile file) {
        return Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf("."));
    }
}
