package com.coc.modi.member.infrastructure;

import com.coc.modi.member.domain.Address;
import com.coc.modi.member.domain.AddressRepository;

import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AddressRepositoryAdapter implements AddressRepository {
	
	private final AddressJpaRepository addressJpaRepository;
	
	@Override
	public List<Address> findByMemberId(Long memberId) {
		
		Sort sort = Sort.by(Sort.Order.desc("isDefault"), Sort.Order.desc("id"));
		
		return addressJpaRepository.findByMemberId(memberId, sort);
	}
	
	@Override
	public Address save(Address address) {
		
		return addressJpaRepository.save(address);
	}
	
	@Override
	public Optional<Address> findById(Long addressId) {
		
		return addressJpaRepository.findById(addressId);
	}
	
	@Override
	public void delete(Address address) {
		
		addressJpaRepository.delete(address);
	}
}
