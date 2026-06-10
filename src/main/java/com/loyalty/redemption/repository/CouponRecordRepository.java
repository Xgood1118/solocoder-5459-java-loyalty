package com.loyalty.redemption.repository;

import com.loyalty.redemption.entity.CouponRecord;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CouponRecordRepository {

    private final Map<String, CouponRecord> store = new ConcurrentHashMap<>();

    public CouponRecord save(CouponRecord record) {
        store.put(record.getCouponId(), record);
        return record;
    }

    public CouponRecord findById(String id) {
        return store.get(id);
    }

    public List<CouponRecord> findByMemberId(String memberId) {
        List<CouponRecord> list = new ArrayList<>();
        for (CouponRecord r : store.values()) {
            if (memberId.equals(r.getMemberId())) {
                list.add(r);
            }
        }
        list.sort(Comparator.comparing(CouponRecord::getIssuedAt).reversed());
        return list;
    }

    public List<CouponRecord> findAll() {
        List<CouponRecord> list = new ArrayList<>(store.values());
        list.sort(Comparator.comparing(CouponRecord::getIssuedAt).reversed());
        return list;
    }
}
