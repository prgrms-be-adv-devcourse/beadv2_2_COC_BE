package com.coc.modi.rental.config;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RentalExclusionConstraintInitializer {

	private static final String CREATE_EXTENSION_SQL = "CREATE EXTENSION IF NOT EXISTS btree_gist";
	private static final String CREATE_CONSTRAINT_SQL = """
			DO $$
			BEGIN
			  IF to_regclass('rental.rental_item') IS NOT NULL THEN
			    IF NOT EXISTS (
			      SELECT 1
			      FROM pg_constraint
			      WHERE conname = 'rental_item_no_overlap'
			        AND conrelid = 'rental.rental_item'::regclass
			    ) THEN
			      ALTER TABLE rental.rental_item
			        ADD CONSTRAINT rental_item_no_overlap
			        EXCLUDE USING gist (
			          product_id WITH =,
			          daterange(start_date, end_date, '[]') WITH &&
			        )
			        WHERE (status IN ('REQUESTED', 'ACCEPTED', 'RENTING', 'PAID'));
			    END IF;
			  END IF;
			END $$;
			""";

	private final JdbcTemplate jdbcTemplate;
	private final AtomicBoolean initialized = new AtomicBoolean(false);

	@Scheduled(
			fixedDelayString = "${rental.constraint-init.delay:30000}",
			initialDelayString = "${rental.constraint-init.initial-delay:5000}"
	)
	public void ensureConstraint() {
		if (initialized.get()) {
			return;
		}

		try {
			jdbcTemplate.execute(CREATE_EXTENSION_SQL);
			jdbcTemplate.execute(CREATE_CONSTRAINT_SQL);
			initialized.set(true);
			log.info("rental exclusion constraint ensured");
		} catch (DataAccessException ex) {
			log.warn("failed to ensure rental exclusion constraint; retrying", ex);
		}
	}
}
