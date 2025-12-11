package com.coc.modi.rental.rental.domain;

import com.coc.modi.common.BaseEntity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "rental_extend", schema = "public")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RentalExtend extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rental_item_id", nullable = false)
	private RentalItem rentalItem;
	
	@Column(name = "old_end_date", nullable = false)
	private LocalDate oldEndDate;
	
	@Column(name = "new_end_date", nullable = false)
	private LocalDate newEndDate;
	
	@Column(name = "extra_days", nullable = false)
	private int extraDays;
	
	@Column(name = "extra_amount", precision = 18, scale = 2, nullable = false)
	private BigDecimal extraAmount;
	
	private RentalExtend(RentalItem rentalItem,
						 LocalDate oldEndDate,
						 LocalDate newEndDate,
						 int extraDays,
						 BigDecimal extraAmount) {
		
		this.rentalItem = rentalItem;
		this.oldEndDate = oldEndDate;
		this.newEndDate = newEndDate;
		this.extraDays = extraDays;
		this.extraAmount = extraAmount;
	}
	
	public static RentalExtend create(RentalItem rentalItem,
									  LocalDate oldEndDate,
									  LocalDate newEndDate,
									  int extraDays,
									  BigDecimal extraAmount) {
		
		return new RentalExtend(rentalItem, oldEndDate, newEndDate, extraDays, extraAmount);
	}
}
