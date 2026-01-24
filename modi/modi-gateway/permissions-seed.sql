DELETE FROM gateway_endpoint_permission;

-- account-service
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/account-service/api/accounts/balance', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/account-service/api/accounts/transactions', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/account-service/api/accounts/withdrawals', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/account-service/api/deposits/pg/request', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/account-service/api/deposits/pg/approve', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/account-service/api/deposits/pg/cancel', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/account-service/api/deposits/pg/payments/fail', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/account-service/api/deposits/pg/config', 'MEMBER');

-- member-service
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/member-service/api/addresses/profile', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/member-service/api/addresses/profile', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PUT', '/member-service/api/addresses/profile/{addressId}', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('DELETE', '/member-service/api/addresses/profile/{addressId}', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/member-service/api/members/profile', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PUT', '/member-service/api/members/profile', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PATCH', '/member-service/api/members/passwords', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('DELETE', '/member-service/api/members', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/member-service/api/auth/oauth2/connect', 'MEMBER');

-- support-service (reviews/notifications/deliveries/admin)
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/support-service/api/reviews', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PATCH', '/support-service/api/reviews/{reviewId}', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('DELETE', '/support-service/api/reviews/{reviewId}', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/support-service/api/reviews', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/support-service/api/reviews/me', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/support-service/api/reviews/summary', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/support-service/api/notifications/stream', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/support-service/api/deliveries', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PATCH', '/support-service/api/deliveries/{rentalItemId}', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/support-service/api/deliveries/{deliveryId}', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/support-service/api/deliveries/rental-items/{rentalItemId}', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/support-service/api/admin/members', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/support-service/api/admin/blacklists', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/support-service/api/admin/blacklists/search', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/support-service/api/admin/blacklists/{memberId}', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/support-service/api/admin/blacklists', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PATCH', '/support-service/api/admin/blacklists/{memberId}/release', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/support-service/api/admin/notices', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/support-service/api/admin/notices/{noticeId}', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/support-service/api/admin/notices', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PATCH', '/support-service/api/admin/notices/{noticeId}', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('DELETE', '/support-service/api/admin/notices/{noticeId}', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PATCH', '/support-service/api/admin/notices/{noticeId}/publish', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PATCH', '/support-service/api/admin/notices/{noticeId}/draft', 'ADMIN');

-- ai-service
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/ai-service/api/ai/recommendations', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/ai-service/api/ai/recommendations/recent', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/ai-service/api/ai/ai/chat-test', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/ai-service/api/ai/descriptions', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/ai-service/api/ai/embeddings/reindex', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/ai-service/api/ai/{productId}/embedding', 'ADMIN');

-- product-service
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/product-service/api/images/upload', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/product-service/api/products/seller', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/product-service/api/products/recent-searches', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/product-service/api/products', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PUT', '/product-service/api/products/{productId}', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PATCH', '/product-service/api/products/{productId}/active', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PATCH', '/product-service/api/products/{productId}/inactive', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('DELETE', '/product-service/api/products/{productId}', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/product-service/api/admin/products/moderation-requests', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/product-service/api/admin/products/{productId}/moderation-requests', 'ADMIN');

-- rental-service
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/rental-service/api/carts', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/rental-service/api/carts/items', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PUT', '/rental-service/api/carts/me/items/{cartItemId}', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('DELETE', '/rental-service/api/carts/me/items/{cartItemId}', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/rental-service/api/rentals', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/rental-service/api/rentals/{rentalId}', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/rental-service/api/rentals', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/rental-service/api/rentals/carts', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/rental-service/api/rentals/{rentalId}/pay', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PATCH', '/rental-service/api/rentals/{rentalItemId}/accept', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PATCH', '/rental-service/api/rentals/{rentalItemId}/reject', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/rental-service/api/rentals/{rentalItemId}/rent', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PATCH', '/rental-service/api/rentals/{rentalItemId}/cancel', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/rental-service/api/rentals/{rentalItemId}/return', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/rental-service/api/rentals/{rentalItemId}/refund', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/rental-service/api/rentals/{rentalItemId}/extend', 'MEMBER');

-- seller-service
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/seller-service/api/sellers', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/seller-service/api/sellers/self', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PUT', '/seller-service/api/sellers/self', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/seller-service/api/sellers/self/rentals', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/seller-service/api/sellers/products/{productId}', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/seller-service/api/chat/rooms', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/seller-service/api/chat/rooms', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/seller-service/api/chat/rooms/{roomId}', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/seller-service/api/chat/rooms/{roomId}/messages', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/seller-service/api/chat/rooms/{roomId}/leave', 'MEMBER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/seller-service/api/settlements/sellers/self', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/seller-service/api/settlements/sellers/self/{sellerSettlementId}', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/seller-service/api/settlements/sellers/self/{sellerSettlementId}/lines', 'SELLER');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PATCH', '/seller-service/api/admin/sellers/{memberId}/approve', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('PATCH', '/seller-service/api/admin/sellers/{memberId}/reject', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/seller-service/api/admin/sellers/registrations', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('GET', '/seller-service/api/admin/settlements/seller-settlements', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/seller-service/api/admin/settlements/seller-settlements/{sellerSettlementId}/pay', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/seller-service/api/admin/settlements/seller-settlements/pay-bulk', 'ADMIN');
INSERT INTO gateway_endpoint_permission (method, path_pattern, roles) VALUES ('POST', '/seller-service/api/admin/settlements/batches/run', 'ADMIN');
