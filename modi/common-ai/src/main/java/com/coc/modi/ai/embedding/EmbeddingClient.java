package com.coc.modi.ai.embedding;

import java.util.List;

public interface EmbeddingClient {

	List<Double> embed(String input);
}
