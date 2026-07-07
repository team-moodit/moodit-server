package com.team.moodit.storage.db.s3.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {
    @Value("${storage.s3.endpoint}") private String endpoint;
    @Value("${storage.s3.access-key}") private String accessKey;
    @Value("${storage.s3.secret-key}") private String secretKey;

    public StaticCredentialsProvider staticCredentialsProvider() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey));
    }

    public S3Configuration s3Configuration() {
        return S3Configuration.builder()
                .pathStyleAccessEnabled(true)
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .httpClient(UrlConnectionHttpClient.builder().build())
                .credentialsProvider(staticCredentialsProvider())
                .region(Region.AP_NORTHEAST_2)
                .endpointOverride(URI.create(endpoint))
                .serviceConfiguration(s3Configuration())
                .overrideConfiguration(ClientOverrideConfiguration.builder().build())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .credentialsProvider(staticCredentialsProvider())
                .region(Region.AP_NORTHEAST_2)
                .endpointOverride(URI.create(endpoint))
                .build();
    }
}
