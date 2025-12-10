package com.coc.modi.product.product.presentation;

import com.coc.modi.common.ApiResponse;
import com.coc.modi.product.product.application.ImageStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/images")
public class ImageUploadController {

    private final ImageStoragePort imageStoragePort;

    // 3-3. 상품 이미지 등록
    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<String>> upload(@RequestParam("file") MultipartFile file,
                                                      @RequestParam(value = "dir", required = false) String dir) {
        String imageUrl = imageStoragePort.upload(file, dir);

        return ResponseEntity.ok(ApiResponse.ok(imageUrl));
    }
}
