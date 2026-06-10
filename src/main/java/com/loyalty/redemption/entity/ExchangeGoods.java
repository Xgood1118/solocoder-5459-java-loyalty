package com.loyalty.redemption.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeGoods {

    private String goodsId;

    private String name;

    private int price;

    private int pointsRequired;

    private int stock;

    private String description;

    private boolean enabled;
}
