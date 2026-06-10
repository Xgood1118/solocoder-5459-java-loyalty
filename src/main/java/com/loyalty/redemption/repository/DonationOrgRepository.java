package com.loyalty.redemption.repository;

import com.loyalty.redemption.entity.DonationOrg;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DonationOrgRepository {

    private final Map<String, DonationOrg> store = new ConcurrentHashMap<>();

    public DonationOrg save(DonationOrg org) {
        store.put(org.getOrgId(), org);
        return org;
    }

    public DonationOrg findById(String id) {
        return store.get(id);
    }

    public Collection<DonationOrg> findAll() {
        return store.values();
    }

    public void deleteById(String id) {
        store.remove(id);
    }
}
