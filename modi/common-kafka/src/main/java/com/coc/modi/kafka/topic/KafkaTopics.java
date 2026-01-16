package com.coc.modi.kafka.topic;

public final class KafkaTopics {
	
	private KafkaTopics() {}
	
	public static final String PRODUCT_EMBEDDING_EVENTS = "product-embedding-events";
	
	public static final String NOTIFICATION_EVENTS = "notification-events";

	public static final String SETTLEMENT_PAYOUT_EVENTS = "settlement-payout-events";

	public static final String SETTLEMENT_PAYOUT_COMPLETED_EVENTS = "settlement-payout-completed-events";

	public static final String SETTLEMENT_PAYOUT_FAILED_EVENTS = "settlement-payout-failed-events";

	public static final String MEMBER_CREATED = "member-created";
	public static final String SELLER_APPROVED = "seller-approved";
	public static final String SELLER_REJECTED = "seller-rejected";
}
