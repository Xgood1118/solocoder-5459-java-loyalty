package com.loyalty.redemption.repository;

import com.loyalty.redemption.entity.ExchangeGoods;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ExchangeGoodsRepository {

    private final Map<String, ExchangeGoods> store = new ConcurrentHashMap<>();

    public ExchangeGoods save(ExchangeGoods goods) {
        store.put(goods.getGoodsId(), goods);
        return goods;
    }

    public ExchangeGoods findById(String id) {
        return store.get(id);
    }

    public Collection<ExchangeGoods> findAll() {
        return store.values();
    }

    public void deleteById(String id) {
        store.remove(id);
    }
}
