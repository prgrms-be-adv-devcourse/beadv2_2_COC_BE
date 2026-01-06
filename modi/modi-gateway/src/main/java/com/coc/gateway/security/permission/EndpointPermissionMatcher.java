package com.coc.gateway.security.permission;

import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.server.PathContainer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.util.pattern.PatternParseException;

@Component
public class EndpointPermissionMatcher {

	private final PathPatternParser parser = new PathPatternParser();
	private final ConcurrentHashMap<String, PathPattern> cache = new ConcurrentHashMap<>();

	public boolean matches(String pattern, String path) {

		if (!StringUtils.hasText(pattern)) {
			return false;
		}

		try {
			PathPattern parsed = cache.computeIfAbsent(pattern, parser::parse);
			return parsed.matches(PathContainer.parsePath(path));
		} catch (PatternParseException ex) {
			return false;
		}
	}
}
