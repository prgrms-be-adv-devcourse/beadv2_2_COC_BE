package com.coc.modi.member.member.infrastructure;

import com.coc.modi.member.member.domain.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;
import java.util.List;

public interface AddressJpaRepository extends JpaRepository<Address, Long> {

    List<Address> findByMemberId(Long memberId, Sort sort);
}
