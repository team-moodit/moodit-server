package com.team.moodit.support.file;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FileUrlResolver {
    @Value("${storage.s3.endpoint}") private String endpoint;
    @Value("${storage.s3.bucket}") private String bucket;

    public String resolve(String objectKey) {
        return String.format("%s/%s/%s", endpoint, bucket, objectKey);
    }
}
