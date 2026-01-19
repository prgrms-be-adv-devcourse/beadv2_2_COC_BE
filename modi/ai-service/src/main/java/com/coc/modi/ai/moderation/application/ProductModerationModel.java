package com.coc.modi.ai.moderation.application;

import com.coc.modi.kafka.event.ProductModerationRequestedEvent;

public interface ProductModerationModel {

	ProductModerationDecisionResult moderate(ProductModerationRequestedEvent event);
}
