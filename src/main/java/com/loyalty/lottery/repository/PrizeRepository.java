package com.loyalty.lottery.repository;

import com.loyalty.lottery.entity.Prize;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class PrizeRepository {

    private final Map<String, Prize> store = new ConcurrentHashMap<>();

    public Prize save(Prize prize) {
        store.put(prize.getPrizeId(), prize);
        return prize;
    }

    public Prize findById(String id) {
        return store.get(id);
    }

    public Collection<Prize> findAll() {
        return store.values();
    }

    public Collection<Prize> findEnabled() {
        return store.values().stream()
                .filter(Prize::isEnabled)
                .toList();
    }

    public void deleteById(String id) {
        store.remove(id);
    }
}
