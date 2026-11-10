package com.utkarsh.careerroadmap.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileService {
    @Autowired
    private Cloudinary cloudinary;
    public String uploadfile(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                ObjectUtils.asMap(
                        "resource_type", "raw"
                )
        );
        String secureUrl = (String) uploadResult.get("secure_url");

        return secureUrl;
    }
}
