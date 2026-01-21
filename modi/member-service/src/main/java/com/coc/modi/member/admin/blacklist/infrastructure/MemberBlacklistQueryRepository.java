package com.coc.modi.member.admin.blacklist.infrastructure;

import com.coc.modi.member.admin.blacklist.domain.BlacklistStatus;
import com.coc.modi.member.admin.blacklist.domain.MemberBlacklist;
import com.coc.modi.member.member.domain.Member;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class MemberBlacklistQueryRepository {

	private static final Set<String> MEMBER_SORT_FIELDS = Set.of("id", "email", "name", "createdAt", "status");

	private final EntityManager entityManager;

	public Page<Member> findMembersByBlacklistStatus(BlacklistStatus status, Pageable pageable) {

		String alias = "m";
		String join = " from Member " + alias;
		String where;
		boolean hasStatus = status != null;

		if (status == BlacklistStatus.SUSPENDED) {
			join += " join " + MemberBlacklist.class.getSimpleName() + " b on " + alias + ".id = b.memberId";
			where = " where b.status = :status";
		} else {
			join += " left join " + MemberBlacklist.class.getSimpleName() + " b on " + alias + ".id = b.memberId";
			where = " where b is null or b.status = :status";
		}

		String orderBy = buildOrderBy(alias, pageable.getSort());
		String selectQuery = "select " + alias + join + where + orderBy;
		String countQuery = "select count(" + alias + ")" + join + where;

		TypedQuery<Member> query = entityManager.createQuery(selectQuery, Member.class);
		TypedQuery<Long> count = entityManager.createQuery(countQuery, Long.class);
		if (hasStatus) {
			query.setParameter("status", status);
			count.setParameter("status", status);
		}

		query.setFirstResult((int) pageable.getOffset());
		query.setMaxResults(pageable.getPageSize());

		List<Member> content = query.getResultList();
		long total = count.getSingleResult();
		return new PageImpl<>(content, pageable, total);
	}

	private String buildOrderBy(String alias, Sort sort) {

		if (sort == null || sort.isUnsorted()) {
			return "";
		}

		List<String> orders = new ArrayList<>();
		for (Sort.Order order : sort) {
			if (!MEMBER_SORT_FIELDS.contains(order.getProperty())) {
				continue;
			}
			String direction = order.isAscending() ? "asc" : "desc";
			orders.add(alias + "." + order.getProperty() + " " + direction);
		}

		if (orders.isEmpty()) {
			return "";
		}

		return " order by " + String.join(", ", orders);
	}
}
