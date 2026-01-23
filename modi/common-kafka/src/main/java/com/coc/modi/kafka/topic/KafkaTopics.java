package com.coc.modi.kafka.topic;

public final class KafkaTopics {
	
	private KafkaTopics() {}

	public static final String PRODUCT_EMBEDDING_EVENTS = "product-embedding-events";
	public static final String PRODUCT_MODERATION_REQUESTED = "product-moderation-requested";
	public static final String PRODUCT_MODERATION_RESULT = "product-moderation-result";
	
	public static final String NOTIFICATION_EVENTS = "notification-events";

	public static final String SELLER_SETTLEMENT_PAYOUT_REQUESTED = "seller-settlement-payout-requested";

	public static final String ACCOUNT_SETTLEMENT_PAYOUT_COMPLETED = "account-settlement-payout-completed";

	public static final String ACCOUNT_SETTLEMENT_PAYOUT_FAILED = "account-settlement-payout-failed";

	public static final String REVIEW_SUMMARY_REQUEST_EVENTS = "review-summary-request-events";
	public static final String REVIEW_SUMMARY_RESULT_EVENTS = "review-summary-result-events";

	public static final String MEMBER_CREATED_EVENTS = "member-created-events";
	public static final String MEMBER_ROLE_CHANGED_EVENTS = "member-role-changed-events";
	public static final String SELLER_REGISTRATION_APPROVED = "seller-registration-approved";
	public static final String SELLER_REGISTRATION_REJECTED = "seller-registration-rejected";

	public static final String CART_ITEM_EVENTS = "cart-item-events";
	public static final String RENTAL_RETURNED_EVENTS = "rental-returned-events";
}
