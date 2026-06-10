package com.loyalty.lottery.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prize {

    private String prizeId;

    private String name;

    private String type;

    private int value;

    private double probability;

    private int quantity;

    private int claimed;

    private boolean enabled;
}
