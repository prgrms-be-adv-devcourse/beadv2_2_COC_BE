package com.coc.modi.product.searchlog.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface KeywordDictionaryRepository extends JpaRepository<KeywordDictionary, Long> {

	List<KeywordDictionary> findBySourceInAndActiveTrue(Collection<String> sources);
}
