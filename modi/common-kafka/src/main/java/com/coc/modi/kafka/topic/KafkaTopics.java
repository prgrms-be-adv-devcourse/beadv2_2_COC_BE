package com.coc.modi.kafka.topic;

public final class KafkaTopics {
	
	private KafkaTopics() {}
	
	public static final String PRODUCT_EMBEDDING_EVENTS = "product-embedding-events";
	
	public static final String NOTIFICATION_EVENTS = "notification-events";

	public static final String SELLER_SETTLEMENT_PAYOUT_REQUESTED = "seller-settlement-payout-requested";

	public static final String ACCOUNT_SETTLEMENT_PAYOUT_COMPLETED = "account-settlement-payout-completed";

	public static final String ACCOUNT_SETTLEMENT_PAYOUT_FAILED = "account-settlement-payout-failed";

	public static final String MEMBER_CREATED = "member-created";
	public static final String SELLER_APPROVED = "seller-approved";
	public static final String SELLER_REJECTED = "seller-rejected";
}
