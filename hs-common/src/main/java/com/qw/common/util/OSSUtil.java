package com.qw.common.util;

import com.aliyun.oss.OSSClient;
import com.qw.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
public class OSSUtil {
    @Value("${aliyun.oss.endpoint}")
    String endpoint;
    @Value("${aliyun.oss.access-key-id}")
    String accessKeyId;
    @Value("${aliyun.oss.access-key-secret}")
    String accessKeySecret;
    @Value("${aliyun.oss.bucket-name}")
    String bucketName;
    @Value("${aliyun.oss.base-url}")
    String baseUrl;

    public String upload(MultipartFile file) {
        String ext = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID() + ext;
        OSSClient ossClient = new OSSClient(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(bucketName, fileName, file.getInputStream());
        } catch (IOException e) {
            log.error("oss upload error", e);
            throw new BizException("上传失败");
        } finally {
            ossClient.shutdown();
        }
        return baseUrl + fileName;
    }
}
