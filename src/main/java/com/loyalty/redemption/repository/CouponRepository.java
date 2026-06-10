package com.loyalty.redemption.repository;

import com.loyalty.redemption.entity.Coupon;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class CouponRepository {

    private final Map<String, Coupon> store = new ConcurrentHashMap<>();

    public Coupon save(Coupon coupon) {
        store.put(coupon.getCouponTemplateId(), coupon);
        return coupon;
    }

    public Coupon findById(String id) {
        return store.get(id);
    }

    public Collection<Coupon> findAll() {
        return store.values();
    }

    public void deleteById(String id) {
        store.remove(id);
    }
}
