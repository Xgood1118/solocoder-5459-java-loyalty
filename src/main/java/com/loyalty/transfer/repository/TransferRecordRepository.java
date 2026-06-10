package com.loyalty.transfer.repository;

import com.loyalty.transfer.entity.TransferRecord;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class TransferRecordRepository {

    private final Map<String, TransferRecord> store = new ConcurrentHashMap<>();

    public TransferRecord save(TransferRecord record) {
        store.put(record.getTransferId(), record);
        return record;
    }

    public TransferRecord findById(String id) {
        return store.get(id);
    }

    public List<TransferRecord> findAll() {
        List<TransferRecord> list = new ArrayList<>(store.values());
        list.sort(Comparator.comparing(TransferRecord::getCreatedAt).reversed());
        return list;
    }

    public List<TransferRecord> findByMemberId(String memberId) {
        List<TransferRecord> list = new ArrayList<>();
        for (TransferRecord r : store.values()) {
            if (memberId.equals(r.getFromMemberId()) || memberId.equals(r.getToMemberId())) {
                list.add(r);
            }
        }
        list.sort(Comparator.comparing(TransferRecord::getCreatedAt).reversed());
        return list;
    }
}
