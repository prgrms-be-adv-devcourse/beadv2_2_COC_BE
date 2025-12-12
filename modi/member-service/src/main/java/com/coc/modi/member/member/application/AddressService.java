package com.coc.modi.member.member.application;

import com.coc.modi.member.member.application.dto.AddressCreateCommand;
import com.coc.modi.member.member.application.dto.AddressListResponse;
import com.coc.modi.member.member.application.dto.AddressResponse;
import com.coc.modi.member.member.application.dto.AddressUpdateCommand;
import com.coc.modi.member.member.domain.Address;
import com.coc.modi.member.member.domain.AddressRepository;
import com.coc.modi.member.member.domain.Member;
import com.coc.modi.member.member.domain.MemberRepository;
import com.coc.modi.member.member.exception.AddressNotFoundException;
import com.coc.modi.member.member.exception.MemberNotFoundException;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final AddressRepository addressRepository;
    private final MemberRepository memberRepository;

	// 내 주소 조회
    @Transactional(readOnly = true)
    public AddressListResponse getProfileAddresses(Long memberId) {

        List<AddressResponse> addressResponses = addressRepository.findByMemberId(memberId)
                .stream()
                .map(AddressResponse::from)
                .toList();

        return AddressListResponse.from(addressResponses);
    }

	// 내 주소 등록
    @Transactional
    public void createAddress(AddressCreateCommand command) {

        Member member = memberRepository.findById(command.memberId())
                .orElseThrow(() -> new MemberNotFoundException(command.memberId()));

        if (command.isDefault()) {

            addressRepository.findByMemberId(member.getId())
                    .forEach(address -> address.updateDefault(false));
        }

        Address address = Address.create(
                member,
                command.addressLabel(),
                command.recipientName(),
                command.recipientPhone(),
                command.type(),
                command.postcode(),
                command.roadAddress(),
                command.detailAddress(),
                command.isDefault()
        );

        addressRepository.save(address);
    }

	// 내 주소 수정
    @Transactional
    public void updateAddress(AddressUpdateCommand command) {

		// 사용자 주소 검증
        Address address = validateAddressOwner(command.memberId(), command.addressId());

        if (command.isDefault()) {

            addressRepository.findByMemberId(command.memberId())
                    .forEach(existing -> existing.updateDefault(existing.getId().equals(address.getId())));
			
        } else if (address.isDefault()) {

            address.updateDefault(false);
        }

        address.update(
                command.addressLabel(),
                command.recipientName(),
                command.recipientPhone(),
                command.type(),
                command.postcode(),
                command.roadAddress(),
                command.detailAddress()
        );
    }

	// 내 주소 삭제
    @Transactional
    public void deleteAddress(Long memberId, Long addressId) {

        Address address = validateAddressOwner(memberId, addressId);

        addressRepository.delete(address);
    }

	// 요청한 사용자 주소인지 검증
    private Address validateAddressOwner(Long memberId, Long addressId) {

        return addressRepository.findById(addressId)
                .filter(address -> address.getMember().getId().equals(memberId))
                .orElseThrow(() -> new AddressNotFoundException(addressId));
    }
}
