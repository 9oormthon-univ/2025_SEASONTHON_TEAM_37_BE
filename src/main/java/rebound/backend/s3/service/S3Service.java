package rebound.backend.s3.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class S3Service {

    private final AmazonS3 s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String uploadFile(MultipartFile file) throws IOException {
        String objectKey = generateFileName(file);

        try {
            ObjectMetadata metadata = buildMetadata(file);

            PutObjectRequest req = new PutObjectRequest(
                    bucketName, objectKey, file.getInputStream(), metadata
            ).withCannedAcl(CannedAccessControlList.PublicRead);

            s3Client.putObject(req);

            String region = s3Client.getRegionName();
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, objectKey);

        } catch (AmazonServiceException e) {
            throw new IOException("S3 error: " + e.getErrorMessage(), e);
        } catch (SdkClientException e) {
            throw new IOException("Client error when uploading to S3", e);
        }
    }

    private ObjectMetadata buildMetadata(MultipartFile file) {
        ObjectMetadata meta = new ObjectMetadata();
        String contentType = file.getContentType();
        meta.setContentType(contentType != null ? contentType : "application/octet-stream");
        meta.setContentLength(file.getSize());
        return meta;
    }

    private String generateFileName(MultipartFile file) {
        String original = Objects.requireNonNullElse(file.getOriginalFilename(), "file");
        return UUID.randomUUID() + "-" + original;
    }
}
