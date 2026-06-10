package com.loyalty.lottery.repository;

import com.loyalty.lottery.entity.LotteryRecord;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class LotteryRecordRepository {

    private final Map<String, LotteryRecord> store = new ConcurrentHashMap<>();

    public LotteryRecord save(LotteryRecord record) {
        store.put(record.getRecordId(), record);
        return record;
    }

    public LotteryRecord findById(String id) {
        return store.get(id);
    }

    public List<LotteryRecord> findAll() {
        List<LotteryRecord> list = new ArrayList<>(store.values());
        list.sort(Comparator.comparing(LotteryRecord::getCreatedAt).reversed());
        return list;
    }

    public List<LotteryRecord> findByMemberId(String memberId) {
        List<LotteryRecord> list = new ArrayList<>();
        for (LotteryRecord r : store.values()) {
            if (memberId.equals(r.getMemberId())) {
                list.add(r);
            }
        }
        list.sort(Comparator.comparing(LotteryRecord::getCreatedAt).reversed());
        return list;
    }

    public int count() {
        return store.size();
    }
}
