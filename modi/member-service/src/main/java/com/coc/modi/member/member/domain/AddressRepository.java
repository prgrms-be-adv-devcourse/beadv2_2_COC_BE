package com.coc.modi.member.member.domain;

import java.util.List;
import java.util.Optional;

public interface AddressRepository {

    List<Address> findByMemberId(Long memberId);

    Address save(Address address);

    Optional<Address> findById(Long addressId);

    void delete(Address address);
}
