package com.coc.modi.ai.recommendation.application;

import com.coc.modi.ai.chat.application.ChatService;
import com.coc.modi.ai.recommendation.presentation.dto.ProductDescriptionRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductDescriptionService {

    private final ChatService chatService;

    public String recommendDescription(ProductDescriptionRequest request) {
        if (request == null) {
            return "";
        }
        String prompt = buildPrompt(request);
        try {
            return chatService.chat(prompt).content();
        } catch (Exception ex) {
            log.warn("상품 설명 생성 실패. productName={}", request.productName(), ex);
            return "요청하신 상품에 대한 설명입니다.";
        }
    }

    private String buildPrompt(ProductDescriptionRequest request) {
        return """
                상품명: %s
                카테고리: %s
                스펙:
                %s

                위 정보를 바탕으로 판매자가 등록하는 상세 설명 문구를 작성해줘.
                스펙에 근거해 성능/특징/사용 상황을 구체적으로 설명해줘.
                안내/요청/추가 확인/조건 제시는 하지 마.
                질문하지 말고, 이모지/해시태그/목록 없이 2~3문장으로 작성해줘.
                """.formatted(
                request.productName(),
                request.category(),
                formatSpecs(request.specs())
        ).trim();
    }

    private String formatSpecs(java.util.Map<String, String> specs) {
        if (specs == null || specs.isEmpty()) {
            return "-";
        }
        StringBuilder builder = new StringBuilder();
        for (java.util.Map.Entry<String, String> entry : specs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || key.isBlank()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append('\n');
            }
            if (value == null || value.isBlank()) {
                builder.append(key.trim()).append(": -");
            } else {
                builder.append(key.trim()).append(": ").append(value.trim());
            }
        }
        return builder.length() == 0 ? "-" : builder.toString();
    }
}
