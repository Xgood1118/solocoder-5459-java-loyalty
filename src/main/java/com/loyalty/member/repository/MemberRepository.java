package com.loyalty.member.repository;

import com.loyalty.member.entity.Member;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class MemberRepository {

    private final Map<String, Member> memberStore = new ConcurrentHashMap<>();

    public Member save(Member member) {
        memberStore.put(member.getMemberId(), member);
        return member;
    }

    public Member findById(String memberId) {
        return memberStore.get(memberId);
    }

    public boolean existsById(String memberId) {
        return memberStore.containsKey(memberId);
    }

    public void deleteById(String memberId) {
        memberStore.remove(memberId);
    }

    public Collection<Member> findAll() {
        return memberStore.values();
    }

    public int count() {
        return memberStore.size();
    }
}
