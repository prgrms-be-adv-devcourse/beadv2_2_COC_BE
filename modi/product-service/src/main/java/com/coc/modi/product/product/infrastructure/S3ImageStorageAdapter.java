package com.coc.modi.product.product.infrastructure;

import com.coc.modi.product.product.exception.ProductInternalException;
import com.coc.modi.product.product.exception.ProductInvalidInputException;

import com.coc.modi.product.product.application.ImageStoragePort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3ImageStorageAdapter implements ImageStoragePort {
	
	private final S3Client s3Client;
	private static final Tika TIKA = new Tika();
	
	@Value("${cloud.aws.s3.bucket}")
	private String bucket;
	
	@Value("${cloud.aws.s3.dir:}")
	private String dir;
	
	@Value("${product.upload.max-bytes:5242880}")
	private long maxBytes;
	
	@Value("#{${product.upload.allowed-mime:{'image/jpeg':'.jpg','image/png':'.png','image/webp':'.webp'}}}")
	private Map<String, String> allowedMime;
	
	private static final Pattern DIR_PATTERN = Pattern.compile("^[a-zA-Z0-9/_-]{1,50}$");
	
	@Override
	public String upload(MultipartFile file, String dirName) {
		
		validateBeforeUpload(file);
		
		String safeDirName = sanitizeDirName(dirName);
		
		byte[] bytes = toBytes(file);
		
		String detectedMime = detectMime(bytes, file.getOriginalFilename());
		String ext = allowedMime.get(detectedMime);
		
		if (ext == null) {
			
			throw new ProductInvalidInputException("허용되지 않는 파일 형식: " + detectedMime);
		}
		
		String key = buildKey(safeDirName, ext);
		
		try {
			
			PutObjectRequest put = PutObjectRequest.builder()
					.bucket(bucket)
					.key(key)
					.contentType(detectedMime)
					.contentLength((long)bytes.length)
					.build();
			
			s3Client.putObject(put, RequestBody.fromBytes(bytes));
			
			validateAfterUpload(key, bytes.length, detectedMime);
			
			String url = getUrl(key);
			
			log.info("S3 업로드 완료: key={}, url={}, size={}, mime={}", key, url, bytes.length, detectedMime);
			
			return url;
		} catch (Exception e) {
			
			log.error("S3 업로드 실패: bucket={}, key={}, dirName={}, size={}, mime={}",
					bucket, key, safeDirName, bytes.length, detectedMime, e);
			
			throw new ProductInternalException("S3 이미지 업로드 중 오류가 발생했습니다.", e);
		}
	}
	
	private String detectMime(byte[] bytes, String originalFilename) {
		
		try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
			
			return TIKA.detect(bais, originalFilename);
		} catch (IOException e) {
			
			throw new ProductInvalidInputException("파일 MIME 판정에 실패했습니다.");
		}
	}
	
	private String buildKey(String dirName, String ext) {
		
		StringBuilder sb = new StringBuilder();
		
		if (dir != null && !dir.isBlank()) {
			sb.append(trimSlashes(dir)).append("/");
		}
		
		if (dirName != null && !dirName.isBlank()) {
			sb.append(trimSlashes(dirName)).append("/");
		}
		
		sb.append(UUID.randomUUID());
		
		if (ext != null && !ext.isBlank()) {
			sb.append(ext);
		}
		
		return sb.toString();
	}
	
	// 업로드 전 파일 검증
	private void validateBeforeUpload(MultipartFile file) {
		
		if (file == null || file.isEmpty()) {
			
			throw new ProductInvalidInputException("업로드할 파일이 없습니다.");
		}
		
		long size = file.getSize();
		
		if (size <= 0) {
			throw new ProductInvalidInputException("파일 크기가 올바르지 않습니다: " + size);
			
		}
		
		if (size > maxBytes) {
			
			throw new ProductInvalidInputException("파일 크기 초과: " + size);
		}
	}
	
	// 업로드 후 파일 검증
	private void validateAfterUpload(String key, long expectedSize, String expectedMime) {
		
		HeadObjectResponse head = s3Client.headObject(b -> b.bucket(bucket).key(key));
		
		long actualSize = head.contentLength();
		String actualMime = head.contentType();
		
		boolean sizeOk = actualSize == expectedSize;
		boolean mimeOk = actualMime.equalsIgnoreCase(expectedMime);
		
		if (!sizeOk || !mimeOk) {
			
			log.error("S3 업로드 검증 실패: key={}, expectedSize={}, actualSize={}, expectedMime={}, actualMime={}",
					key, expectedSize, actualSize, expectedMime, actualMime);
			
			throw new ProductInternalException("S3 업로드 검증 실패: key=" + key, null);
		}
	}
	
	private String sanitizeDirName(String dirName) {
		
		if (dirName == null || dirName.isBlank()) {
			
			return null;
		}
		
		String normalizedDirName = dirName.trim();
		
		while (normalizedDirName.startsWith("/")) {
			
			normalizedDirName = normalizedDirName.substring(1);
		}
		
		while (normalizedDirName.endsWith("/")) {
			
			normalizedDirName = normalizedDirName.substring(0, normalizedDirName.length() - 1);
		}
		
		if (normalizedDirName.isBlank()) {
			
			return null;
		}
		
		if (normalizedDirName.contains("..")) {
			
			throw new ProductInvalidInputException("허용되지 않는 dir 값입니다.");
		}
		
		if (!DIR_PATTERN.matcher(normalizedDirName).matches()) {
			
			throw new ProductInvalidInputException("허용되지 않는 dir 형식입니다. (영문/숫자/_-/ 만 허용)");
		}
		
		return normalizedDirName;
	}
	
	private byte[] toBytes(MultipartFile file) {
		
		try {
			
			return file.getBytes();
		} catch (IOException e) {
			
			throw new ProductInvalidInputException("파일을 읽을 수 없습니다.");
		}
	}
	
	private String trimSlashes(String str) {
		
		String trimmed = str.trim();
		
		while (trimmed.startsWith("/")) {
			
			trimmed = trimmed.substring(1);
		}
		
		while (trimmed.endsWith("/")) {
			
			trimmed = trimmed.substring(0, trimmed.length() - 1);
		}
		
		return trimmed;
	}
	
	private String getUrl(String key) {
		
		return "https://" + bucket + ".s3.amazonaws.com/" + key;
	}
}
