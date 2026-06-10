package com.loyalty.common.enums;

import lombok.Getter;

@Getter
public enum ProductCategory {
    NORMAL("normal", "普通商品", 1.0),
    FRESH("fresh", "生鲜", 0.5),
    LUXURY("luxury", "奢侈品", 2.0),
    TOBACCO_ALCOHOL("tobacco_alcohol", "烟酒", 0.0);

    private final String code;
    private final String name;
    private final double multiplier;

    ProductCategory(String code, String name, double multiplier) {
        this.code = code;
        this.name = name;
        this.multiplier = multiplier;
    }

    public static ProductCategory fromCode(String code) {
        for (ProductCategory cat : values()) {
            if (cat.code.equalsIgnoreCase(code)) {
                return cat;
            }
        }
        return NORMAL;
    }
}
