package com.qw.common.service;

import com.qw.common.exception.BizException;
import com.qw.common.util.OSSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
public class FileService {
    @Autowired
    OSSUtil ossUtil;

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new BizException("不支持的文件类型");
        }
        String ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BizException("不支持的文件类型");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException("文件大小不能超过5MB");
        }
        return ossUtil.upload(file);
    }
}
