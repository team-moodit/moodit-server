package com.team.moodit.support.file;

import com.team.moodit.storage.db.s3.S3Uploader;
import com.team.moodit.support.error.ApiException;
import com.team.moodit.support.error.ErrorType;
import java.io.IOException;
import java.util.UUID;
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

    @Value("${storage.s3.endpoint}") private String endpoint;
    @Value("${storage.s3.bucket}") private String bucket;

    public UploadResult upload(Long userId, MultipartFile file) {
        try {
            String objectKey = uploadToS3(file);

            // TODO: FileEntity 등 저장 로직 추가

            return new UploadResult(
                    1L,
                    endpoint + "/" + bucket + "/" + objectKey
            );
        } catch (IOException e) {
            log.error("[FileUploader] userId: {}, filename: {}. size: {} bytes, message: {}", userId, file.getOriginalFilename(), file.getSize(), e.getMessage(), e);
            throw new ApiException(ErrorType.FILE_UPLOADING_FAILED);
        }
    }

    private String uploadToS3(MultipartFile file) throws IOException {
        String objectKey = UUID.randomUUID() + "_" + file.getOriginalFilename();
        return s3Uploader.uploadFile(file, objectKey);
    }
}
