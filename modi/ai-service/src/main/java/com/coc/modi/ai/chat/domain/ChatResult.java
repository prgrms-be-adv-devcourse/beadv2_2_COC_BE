package com.coc.modi.ai.chat.domain;

import java.util.Map;

public record ChatResult(String content, Map<String, Object> metadata) {
}
