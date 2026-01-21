package com.coc.modi.ai.embedding.domain;

import java.util.List;

public interface EmbeddingClient {

	List<Double> embed(String input);
}
