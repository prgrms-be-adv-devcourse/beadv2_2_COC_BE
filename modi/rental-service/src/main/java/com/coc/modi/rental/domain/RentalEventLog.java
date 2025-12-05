package com.coc.modi.rental.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "rental_event_log", schema = "public")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RentalEventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", length = 30, nullable = false)
    private RentalEventType eventType;

    @Lob
    @Column(name = "payload_json", columnDefinition = "TEXT", nullable = false)
    private String payloadJson;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public static RentalEventLog create(Rental rental, RentalEventType eventType, String payloadJson) {

        RentalEventLog log = new RentalEventLog();
        log.rental = rental;
        log.eventType = eventType;
        log.payloadJson = payloadJson;
        log.createdAt = LocalDateTime.now();
        return log;
    }
}
