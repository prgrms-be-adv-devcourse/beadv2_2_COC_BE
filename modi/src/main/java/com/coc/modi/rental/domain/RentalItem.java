package com.coc.modi.rental.domain;

import com.coc.modi.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "rental_item", schema = "public")
public class RentalItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @OneToMany(mappedBy = "rentalItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RentalExtend> extendsHistory = new ArrayList<>();

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private RentalItemStatus status;

    @Column(name = "unit_price", precision = 18, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "returned_at")
    private LocalDateTime returnedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;




}
