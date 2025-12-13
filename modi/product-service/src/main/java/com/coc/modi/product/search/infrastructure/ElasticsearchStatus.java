package com.coc.modi.product.search.infrastructure;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

@Component
public class ElasticsearchStatus {
	
	public enum State {UNKNOWN, AVAILABLE, UNAVAILABLE}
	
	private final AtomicReference<State> state = new AtomicReference<>(State.UNKNOWN);
	private final AtomicReference<Throwable> lastError = new AtomicReference<>();
	
	public void markAvailable() {
		
		state.set(State.AVAILABLE);
		lastError.set(null);
	}
	
	public void markUnavailable(Throwable t) {
		
		state.set(State.UNAVAILABLE);
		lastError.set(t);
	}
	
	public State getState() {
		
		return state.get();
	}
	
	public Optional<Throwable> getLastError() {
		
		return Optional.ofNullable(lastError.get());
	}
	
	public boolean isAvailable() {
		
		return state.get() == State.AVAILABLE;
	}
}
