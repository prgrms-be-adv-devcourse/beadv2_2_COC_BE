package com.coc.modi.rental.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@Table(name = "rental_extend", schema = "public")
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
}
