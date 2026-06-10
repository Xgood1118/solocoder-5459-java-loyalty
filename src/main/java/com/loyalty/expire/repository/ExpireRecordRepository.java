package com.loyalty.expire.repository;

import com.loyalty.expire.entity.ExpireRecord;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ExpireRecordRepository {

    private final Map<String, ExpireRecord> store = new ConcurrentHashMap<>();

    public ExpireRecord save(ExpireRecord record) {
        store.put(record.getRecordId(), record);
        return record;
    }

    public ExpireRecord findById(String id) {
        return store.get(id);
    }

    public List<ExpireRecord> findAll() {
        List<ExpireRecord> list = new ArrayList<>(store.values());
        list.sort(Comparator.comparing(ExpireRecord::getExecutedAt).reversed());
        return list;
    }

    public boolean existsByYear(int year) {
        return store.values().stream().anyMatch(r -> r.getYear() == year && "SUCCESS".equals(r.getStatus()));
    }
}
