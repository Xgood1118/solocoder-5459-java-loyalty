package com.loyalty.point.repository;

import com.loyalty.point.entity.PointTransaction;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class PointTransactionRepository {

    private final Map<Long, PointTransaction> transactionStore = new ConcurrentHashMap<>();
    private final Map<String, Long> orderIdIndex = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    public PointTransaction save(PointTransaction transaction) {
        if (transaction.getTransactionId() == null) {
            transaction.setTransactionId(idGenerator.incrementAndGet());
        }
        transactionStore.put(transaction.getTransactionId(), transaction);
        if (transaction.getOrderId() != null) {
            orderIdIndex.put(transaction.getOrderId(), transaction.getTransactionId());
        }
        return transaction;
    }

    public PointTransaction findById(Long id) {
        return transactionStore.get(id);
    }

    public boolean existsByOrderId(String orderId) {
        return orderIdIndex.containsKey(orderId);
    }

    public PointTransaction findByOrderId(String orderId) {
        Long id = orderIdIndex.get(orderId);
        return id != null ? transactionStore.get(id) : null;
    }

    public List<PointTransaction> findByMemberId(String memberId) {
        List<PointTransaction> result = new ArrayList<>();
        for (PointTransaction tx : transactionStore.values()) {
            if (memberId.equals(tx.getMemberId())) {
                result.add(tx);
            }
        }
        result.sort(Comparator.comparing(PointTransaction::getCreatedAt).reversed());
        return result;
    }

    public List<PointTransaction> findAll() {
        List<PointTransaction> result = new ArrayList<>(transactionStore.values());
        result.sort(Comparator.comparing(PointTransaction::getCreatedAt).reversed());
        return result;
    }

    public int count() {
        return transactionStore.size();
    }

    public long getMaxTransactionId() {
        return idGenerator.get();
    }
}
