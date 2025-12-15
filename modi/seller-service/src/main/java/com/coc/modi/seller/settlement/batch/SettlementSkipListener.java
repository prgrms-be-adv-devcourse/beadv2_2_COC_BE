package com.coc.modi.seller.settlement.batch;

import lombok.extern.slf4j.Slf4j;

import org.springframework.batch.core.SkipListener;

@Slf4j
public class SettlementSkipListener implements SkipListener<Object, Object> {
	
	@Override
	public void onSkipInRead(Throwable t) {
		
		log.warn("Settlement batch skip in read", t);
	}
	
	@Override
	public void onSkipInWrite(Object item, Throwable t) {
		
		log.warn("Settlement batch skip in write. item={}", item, t);
	}
	
	@Override
	public void onSkipInProcess(Object item, Throwable t) {
		
		log.warn("Settlement batch skip in process. item={}", item, t);
	}
}
