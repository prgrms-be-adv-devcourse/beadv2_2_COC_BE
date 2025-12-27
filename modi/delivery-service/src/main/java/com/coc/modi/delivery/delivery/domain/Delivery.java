package com.coc.modi.delivery.delivery.domain;

import java.time.LocalDateTime;

import com.coc.modi.common.BaseEntity;
import com.coc.modi.delivery.delivery.infrastructure.TrackingResult;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "delivery", schema = "public")
public class Delivery extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "rental_item_id", nullable = false)
	private Long rentalItemId;
	
	@Column(name = "carrier_code", length = 30, nullable = false)
	private String carrierCode;
	
	@Column(name = "tracking_number", length = 50, nullable = false)
	private String trackingNumber;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "status", length = 20, nullable = false)
	private DeliveryStatus status;
	
	@Column(name = "status_raw", length = 50)
	private String statusRaw;
	
	@Column(name = "last_tracked_at")
	private LocalDateTime lastTrackedAt;
	
	protected Delivery() {
	
	}
	
	private Delivery(Long rentalItemId,
					 String carrierCode,
					 String trackingNumber,
					 DeliveryStatus status,
					 String statusRaw) {
		
		this.rentalItemId = rentalItemId;
		this.carrierCode = carrierCode;
		this.trackingNumber = trackingNumber;
		this.status = status != null ? status : DeliveryStatus.REGISTERED;
		this.statusRaw = statusRaw;
		this.lastTrackedAt = null;
	}
	
	public static Delivery create(Long rentalItemId,
								  String carrierCode,
								  String trackingNumber) {
		
		return new Delivery(rentalItemId, carrierCode, trackingNumber, null, null);
	}
	
	public void applyTrackingResult(DeliveryStatus status, TrackingResult result) {
		
		this.status = status;
		this.statusRaw = (result != null) ? result.rawStatus() : null;
	}
	
	public void markTrackedNow() {
		
		this.lastTrackedAt = LocalDateTime.now();
	}
}
