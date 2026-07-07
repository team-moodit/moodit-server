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
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileUploader {
    private final S3Uploader s3Uploader;
    private final FileRepository fileRepository;
    private final ObjectKeyGenerator objectKeyGenerator;

    public UploadResult createPresignedUrl(Long userId, ObjectResourceType resourceType, String fileName) {
        try {
            String rawExtension = extractExtension(fileName);
            String objectKey = objectKeyGenerator.generate(resourceType, rawExtension);

            String extension = rawExtension.replace(".", "").replace(" ", "+");

            FileEntity savedFile = fileRepository.save(
                    new FileEntity(
                            userId,
                            resourceType,
                            objectKey,
                            fileName,
                            "image/" + extension
                    )
            );

            return new UploadResult(
                    savedFile.getId(),
                    s3Uploader.createPresignedUrl(objectKey, extension)
            );
        } catch (Exception e) {
            log.error("[FileUploader] userId: {}, fileName: {}. message: {}", userId, fileName, e.getMessage(), e);
            throw new ApiException(ErrorType.FILE_UPLOADING_FAILED);
        }
    }

    private String extractExtension(String fileName) {
        return Objects.requireNonNull(fileName).substring(fileName.lastIndexOf("."));
    }
}
