package com.coc.modi.product.searchlog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "keyword_dictionary")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class KeywordDictionary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 200)
	private String source;

	@Column(nullable = false, length = 200)
	private String target;

	@Column(nullable = false, length = 20)
	private String type;

	@Column(nullable = false)
	private Integer priority;

	@Column(nullable = false)
	private Boolean active;
}
