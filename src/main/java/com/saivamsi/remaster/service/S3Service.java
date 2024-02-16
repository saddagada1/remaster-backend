package com.saivamsi.remaster.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    @Value("${S3_BUCKET_NAME}")
    private String s3BucketName;

    @Value("${CLOUDFRONT_DOMAIN}")
    private String cloudfrontDomain;

    public String putObject(String key, byte[] file) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(s3BucketName)
                .key(key)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(file));
        return cloudfrontDomain + key;
    }

    public void deleteObject(String key, Boolean trim) {
        if (trim) {
            key = key.replace(cloudfrontDomain, "");
        }
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(s3BucketName)
                .key(key)
                .build();
        s3Client.deleteObject(request);
    }
}
