package com.sirius.posterworld.services;



import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class S3ImageService {

    @Autowired
    private AmazonS3 s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    public String uploadImage(MultipartFile file) throws IOException {
        String uniqueFileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        File tempFile = convertMultipartFileToFile(file);
        s3Client.putObject(new PutObjectRequest(bucketName, uniqueFileName, tempFile));
        tempFile.delete(); // Clean up temporary file
        return s3Client.getUrl(bucketName, uniqueFileName).toString();
    }

    // Helper method to convert MultipartFile to File
    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        Path tempFilePath = Files.createTempFile("temp", file.getOriginalFilename());
        Files.write(tempFilePath, file.getBytes());
        return tempFilePath.toFile();
    }

    public void deleteImage(String imageUrl) {
        // Extract the key (file name) from the S3 URL
        String key = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        s3Client.deleteObject(bucketName, key);
    }
}
