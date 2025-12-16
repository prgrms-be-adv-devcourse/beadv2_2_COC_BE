package com.coc.modi.product.product.infrastructure;

import com.coc.modi.product.product.exception.ProductInternalException;
import com.coc.modi.product.product.exception.ProductInvalidInputException;

import com.coc.modi.product.product.application.ImageStoragePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ImageStorageAdapter implements ImageStoragePort {
	
	private final S3Client s3Client;
	
	@Value("${cloud.aws.s3.bucket}")
	private String bucket;
	
	@Value("${cloud.aws.s3.dir:}")
	private String dir;
	
	@Override
	public String upload(MultipartFile file, String dirName) {
		
		if (file == null) {
			throw new ProductInvalidInputException("업로드할 파일이 없습니다.");
		}
		if (file.isEmpty()) {
			throw new ProductInvalidInputException("파일이 비어 있습니다.");
		}
		
		String originalFilename = file.getOriginalFilename();
		String ext = getExtension(originalFilename);
		String key = buildKey(dirName, ext);
		
		try {
			PutObjectRequest putObjectRequest = PutObjectRequest.builder()
					.bucket(bucket)
					.key(key)
					.contentType(file.getContentType())
					.contentLength(file.getSize())
					.build();
			
			s3Client.putObject(
					putObjectRequest,
					RequestBody.fromInputStream(file.getInputStream(), file.getSize())
			);
			
			String url = getUrl(key);
			
			log.info("S3 업로드 완료: key={}, url={}", key, url);
			
			return url;
		} catch (IOException e) {
			throw new ProductInternalException("S3 이미지 업로드 중 오류가 발생했습니다.", e);
		} catch (Exception e) {
			throw new ProductInternalException("S3 이미지 업로드 중 예상치 못한 오류가 발생했습니다.", e);
		}
	}
	
	private String getExtension(String originalFilename) {
		
		if (originalFilename == null) {
			return "";
		}
		int idx = originalFilename.lastIndexOf('.');
		if (idx == -1) {
			return "";
		}
		return originalFilename.substring(idx);
	}
	
	private String buildKey(String dirName, String ext) {
		
		StringBuilder sb = new StringBuilder();
		
		if (dir != null && !dir.isBlank()) {
			sb.append(dir);
			if (!dir.endsWith("/")) {
				sb.append("/");
			}
		}
		
		if (dirName != null && !dirName.isBlank()) {
			sb.append(dirName);
			if (!dirName.endsWith("/")) {
				sb.append("/");
			}
		}
		
		sb.append(UUID.randomUUID());
		
		if (ext != null && !ext.isBlank()) {
			sb.append(ext);
		}
		
		return sb.toString();
	}
	
	private String getUrl(String key) {
		
		return "https://" + bucket + ".s3.amazonaws.com/" + key;
	}
}
