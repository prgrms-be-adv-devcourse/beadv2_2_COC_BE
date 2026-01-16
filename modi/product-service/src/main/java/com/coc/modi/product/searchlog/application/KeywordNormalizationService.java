package com.coc.modi.product.searchlog.application;

import com.coc.modi.product.searchlog.domain.KeywordDictionary;
import com.coc.modi.product.searchlog.domain.KeywordDictionaryRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class KeywordNormalizationService {

	private static final Pattern NON_WORD = Pattern.compile("[^0-9a-z가-힣]+");
	private static final Pattern MULTI_SPACE = Pattern.compile("\\s+");
	private static final Pattern TOKEN_PATTERN = Pattern.compile("([0-9]+|[a-z가-힣]+)");

	private final KeywordDictionaryRepository keywordDictionaryRepository;

	public String normalize(String input) {
		return normalizeInternal(input, "");
	}

	public String normalizeForSearch(String input) {
		return normalizeInternal(input, " ");
	}

	private String normalizeInternal(String input, String delimiter) {
		if (input == null) {
			return null;
		}
		String trimmed = input.trim();
		if (trimmed.isEmpty()) {
			return null;
		}
		String base = NON_WORD.matcher(trimmed.toLowerCase(Locale.ROOT)).replaceAll(" ");
		base = MULTI_SPACE.matcher(base).replaceAll(" ").trim();
		if (base.isEmpty()) {
			return null;
		}

		List<String> tokens = extractTokens(base);
		if (tokens.isEmpty()) {
			return null;
		}

		Map<String, KeywordDictionary> mapping = resolveMappings(tokens);

		StringBuilder result = new StringBuilder();
		for (String token : tokens) {
			KeywordDictionary dictionary = mapping.get(token);
			if (!result.isEmpty() && !delimiter.isEmpty()) {
				result.append(delimiter);
			}
			result.append(dictionary != null ? dictionary.getTarget() : token);
		}
		if (result.isEmpty()) {
			return null;
		}
		return result.toString();
	}

	private List<String> extractTokens(String base) {
		List<String> tokens = new ArrayList<>();
		Matcher matcher = TOKEN_PATTERN.matcher(base);
		while (matcher.find()) {
			tokens.add(matcher.group(1));
		}
		return tokens;
	}

	private Map<String, KeywordDictionary> resolveMappings(List<String> tokens) {
		List<KeywordDictionary> entries = keywordDictionaryRepository.findBySourceInAndActiveTrue(tokens);
		Map<String, KeywordDictionary> mapping = new HashMap<>();
		for (KeywordDictionary entry : entries) {
			KeywordDictionary existing = mapping.get(entry.getSource());
			if (existing == null || entry.getPriority() > existing.getPriority()) {
				mapping.put(entry.getSource(), entry);
			}
		}
		return mapping;
	}
}
