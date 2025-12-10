package com.coc.modi.product.application;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStoragePort {

    String upload(MultipartFile file, String dirName);
}
