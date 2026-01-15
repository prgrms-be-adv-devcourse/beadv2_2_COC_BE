-- Keyword normalization migration for product_search_log.
-- Assumes product.product_search_log already exists.

ALTER TABLE IF EXISTS product.product_search_log
    ADD COLUMN IF NOT EXISTS keyword_raw VARCHAR(200),
    ADD COLUMN IF NOT EXISTS keyword_norm VARCHAR(200);

CREATE TABLE IF NOT EXISTS product.keyword_dictionary (
    id BIGSERIAL PRIMARY KEY,
    source VARCHAR(200) NOT NULL,
    target VARCHAR(200) NOT NULL,
    type VARCHAR(20) NOT NULL,
    priority INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS keyword_dictionary_source_idx
    ON product.keyword_dictionary (source);

CREATE UNIQUE INDEX IF NOT EXISTS keyword_dictionary_source_type_uidx
    ON product.keyword_dictionary (source, type);

CREATE OR REPLACE FUNCTION product.normalize_keyword_base(input TEXT)
RETURNS TEXT
LANGUAGE sql
IMMUTABLE
AS 'SELECT NULLIF(
        regexp_replace(
            regexp_replace(lower(trim(input)), ''[^0-9a-z가-힣]+'', '' '', ''g''),
            ''\\s+'', '' '', ''g''
        ),
        ''''
    );';

CREATE OR REPLACE FUNCTION product.normalize_keyword(input TEXT)
RETURNS TEXT
LANGUAGE sql
STABLE
AS 'SELECT NULLIF(
        (
            SELECT string_agg(COALESCE(kd.target, t.token), '''' ORDER BY t.ord)
            FROM (
                SELECT m[1] AS token, row_number() OVER () AS ord
                FROM regexp_matches(product.normalize_keyword_base(input), ''([0-9]+|[a-z가-힣]+)'', ''g'') AS m
            ) t
            LEFT JOIN LATERAL (
                SELECT kd.target
                FROM product.keyword_dictionary kd
                WHERE kd.source = t.token
                  AND kd.active = TRUE
                ORDER BY kd.priority DESC
                LIMIT 1
            ) kd ON TRUE
        ),
        ''''
    );';

CREATE OR REPLACE FUNCTION product.normalize_keyword_search(input TEXT)
RETURNS TEXT
LANGUAGE sql
STABLE
AS 'SELECT NULLIF(
        (
            SELECT string_agg(COALESCE(kd.target, t.token), '' '' ORDER BY t.ord)
            FROM (
                SELECT m[1] AS token, row_number() OVER () AS ord
                FROM regexp_matches(product.normalize_keyword_base(input), ''([0-9]+|[a-z가-힣]+)'', ''g'') AS m
            ) t
            LEFT JOIN LATERAL (
                SELECT kd.target
                FROM product.keyword_dictionary kd
                WHERE kd.source = t.token
                  AND kd.active = TRUE
                ORDER BY kd.priority DESC
                LIMIT 1
            ) kd ON TRUE
        ),
        ''''
    );';

INSERT INTO product.keyword_dictionary (source, target, type, priority, active)
VALUES
    ('cam', '카메라', 'SYNONYM', 100, TRUE),
    ('camera', '카메라', 'SYNONYM', 100, TRUE),
    ('캠', '카메라', 'SYNONYM', 100, TRUE),
    ('카메라', '카메라', 'SYNONYM', 100, TRUE),
    ('dslr', '카메라', 'SYNONYM', 90, TRUE),

    ('맥북', '맥북', 'SYNONYM', 100, TRUE),
    ('macbook', '맥북', 'SYNONYM', 100, TRUE),
    ('macbookair', '맥북', 'SYNONYM', 90, TRUE),
    ('macbookpro', '맥북', 'SYNONYM', 90, TRUE),

    ('laptop', '노트북', 'SYNONYM', 100, TRUE),
    ('랩탑', '노트북', 'SYNONYM', 100, TRUE),
    ('노트북', '노트북', 'SYNONYM', 100, TRUE),
    ('그램', '노트북', 'SYNONYM', 90, TRUE),
    ('lg그램', '노트북', 'SYNONYM', 90, TRUE),

    ('버즈', '갤럭시버즈', 'SYNONYM', 100, TRUE),
    ('buds', '갤럭시버즈', 'SYNONYM', 100, TRUE),
    ('buds2', '갤럭시버즈', 'SYNONYM', 90, TRUE),
    ('galaxybuds', '갤럭시버즈', 'SYNONYM', 100, TRUE),
    ('갤럭시버즈', '갤럭시버즈', 'SYNONYM', 100, TRUE),

    ('ctype', 'usbc', 'ABBREV', 100, TRUE),
    ('c타입', 'usbc', 'ABBREV', 100, TRUE),
    ('씨타입', 'usbc', 'ABBREV', 90, TRUE),
    ('usbc', 'usbc', 'ABBREV', 100, TRUE),
    ('typec', 'usbc', 'ABBREV', 90, TRUE),
    ('usbtypec', 'usbc', 'ABBREV', 80, TRUE),

    ('핸드폰', '휴대폰', 'SYNONYM', 100, TRUE),
    ('휴대폰', '휴대폰', 'SYNONYM', 100, TRUE),
    ('폰', '휴대폰', 'SYNONYM', 100, TRUE),
    ('phone', '휴대폰', 'SYNONYM', 90, TRUE),

    ('tablet', '태블릿', 'SYNONYM', 90, TRUE),
    ('태블릿', '태블릿', 'SYNONYM', 100, TRUE),
    ('ipad', '아이패드', 'SYNONYM', 100, TRUE),
    ('아이패드', '아이패드', 'SYNONYM', 100, TRUE),
    ('iphone', '아이폰', 'SYNONYM', 100, TRUE),
    ('iphone15', '아이폰15', 'SYNONYM', 100, TRUE),
    ('아이폰', '아이폰', 'SYNONYM', 100, TRUE),
    ('아이폰15', '아이폰15', 'SYNONYM', 100, TRUE),
    ('갤탭', '갤럭시탭', 'SYNONYM', 100, TRUE),
    ('galaxytab', '갤럭시탭', 'SYNONYM', 100, TRUE),
    ('갤럭시탭', '갤럭시탭', 'SYNONYM', 100, TRUE),

    ('airpods', '에어팟', 'SYNONYM', 100, TRUE),
    ('airpod', '에어팟', 'SYNONYM', 90, TRUE),
    ('에어팟', '에어팟', 'SYNONYM', 100, TRUE),

    ('pro', '프로', 'SYNONYM', 100, TRUE),
    ('max', '맥스', 'SYNONYM', 100, TRUE),
    ('mini', '미니', 'SYNONYM', 100, TRUE),
    ('plus', '플러스', 'SYNONYM', 100, TRUE),
    ('ultra', '울트라', 'SYNONYM', 100, TRUE),

    ('projector', '프로젝터', 'SYNONYM', 100, TRUE),
    ('빔', '프로젝터', 'SYNONYM', 90, TRUE),
    ('빔프로젝터', '프로젝터', 'SYNONYM', 100, TRUE),
    ('프로젝터', '프로젝터', 'SYNONYM', 100, TRUE),

    ('monitor', '모니터', 'SYNONYM', 100, TRUE),
    ('모니터', '모니터', 'SYNONYM', 100, TRUE),
    ('odyssey', '모니터', 'SYNONYM', 80, TRUE),
    ('오디세이', '모니터', 'SYNONYM', 80, TRUE),
    ('ultragear', '모니터', 'SYNONYM', 80, TRUE),
    ('울트라기어', '모니터', 'SYNONYM', 80, TRUE),

    ('keyboard', '키보드', 'SYNONYM', 100, TRUE),
    ('키보드', '키보드', 'SYNONYM', 100, TRUE),
    ('mouse', '마우스', 'SYNONYM', 100, TRUE),
    ('마우스', '마우스', 'SYNONYM', 100, TRUE),
    ('hub', '허브', 'SYNONYM', 90, TRUE),
    ('허브', '허브', 'SYNONYM', 90, TRUE),

    ('speaker', '스피커', 'SYNONYM', 100, TRUE),
    ('스피커', '스피커', 'SYNONYM', 100, TRUE),
    ('헤드폰', '헤드폰', 'SYNONYM', 100, TRUE),
    ('헤드셋', '헤드폰', 'SYNONYM', 90, TRUE),
    ('headphone', '헤드폰', 'SYNONYM', 100, TRUE),
    ('headset', '헤드폰', 'SYNONYM', 90, TRUE),
    ('이어폰', '이어폰', 'SYNONYM', 100, TRUE),
    ('earphone', '이어폰', 'SYNONYM', 90, TRUE),

    ('drone', '드론', 'SYNONYM', 100, TRUE),
    ('드론', '드론', 'SYNONYM', 100, TRUE),
    ('dji', '드론', 'SYNONYM', 80, TRUE),

    ('카매라', '카메라', 'TYPO', 200, TRUE)
ON CONFLICT (source, type) DO UPDATE
SET target = EXCLUDED.target,
    priority = EXCLUDED.priority,
    active = EXCLUDED.active;

-- Backfill from legacy keyword column when present.
UPDATE product.product_search_log
SET keyword_raw = COALESCE(keyword_raw, keyword),
    keyword_norm = COALESCE(keyword_norm, product.normalize_keyword(keyword))
WHERE (keyword_raw IS NULL OR keyword_norm IS NULL)
  AND keyword IS NOT NULL;

CREATE INDEX IF NOT EXISTS product_search_log_keyword_norm_idx
    ON product.product_search_log (keyword_norm);
