package com.team.moodit.storage.db.s3;

import java.io.IOException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Uploader {
    private static final Duration PRESIGNED_URL_EXPIRATION = Duration.ofMinutes(10);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${storage.s3.bucket}")
    private String bucket;

    public String uploadFile(MultipartFile file, String objectKey) throws IOException {
        s3Client.putObject(
                PutObjectRequest.builder()
                        .bucket(bucket)
                        .key(objectKey)
                        .contentType(file.getContentType())
                        .build(),
                RequestBody.fromInputStream(file.getInputStream(), file.getSize())
        );

        return objectKey;
    }

    public String createPresignedUrl(String objectKey, String extension) {
        PresignedPutObjectRequest request = s3Presigner.presignPutObject(
                PutObjectPresignRequest.builder()
                        .signatureDuration(PRESIGNED_URL_EXPIRATION)
                        .putObjectRequest(
                                PutObjectRequest.builder()
                                        .bucket(bucket)
                                        .key(objectKey)
                                        .contentType("image/" + extension.replaceAll("\\.", ""))
                                        .build()
                        ).build()
        );

        return request.url().toString();
    }
}
