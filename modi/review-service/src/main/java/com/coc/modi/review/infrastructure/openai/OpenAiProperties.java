package com.coc.modi.review.infrastructure.openai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties(prefix = "openai")
public class OpenAiProperties {

	@Setter
    private String apiKey;
	@Setter
    private String baseUrl = "https://api.openai.com/v1";
	@Setter
    private String model = "gpt-4.1-mini";
	private final Summary summary = new Summary();

    @Setter
    @Getter
    public static class Summary {

		private int maxLength = 200;
    }
}
