-- request_id 컬럼 및 멱등성 유니크 인덱스 추가 (account 스키마 기준)
ALTER TABLE account.wallet_transaction
	ADD COLUMN IF NOT EXISTS request_id varchar(120);

CREATE UNIQUE INDEX IF NOT EXISTS uq_wallet_tx_type_request_id
	ON account.wallet_transaction (tx_type, request_id);
