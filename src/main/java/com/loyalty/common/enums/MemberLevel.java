package com.loyalty.common.enums;

import lombok.Getter;

@Getter
public enum MemberLevel {
    NORMAL(1, "普卡", 0),
    SILVER(2, "银卡", 1000),
    GOLD(3, "金卡", 5000),
    PLATINUM(4, "白金", 20000);

    private final int code;
    private final String name;
    private final int threshold;

    MemberLevel(int code, String name, int threshold) {
        this.code = code;
        this.name = name;
        this.threshold = threshold;
    }

    public static MemberLevel fromCode(int code) {
        for (MemberLevel level : values()) {
            if (level.code == code) {
                return level;
            }
        }
        return NORMAL;
    }

    public static MemberLevel fromPoints(int totalPoints) {
        MemberLevel result = NORMAL;
        for (MemberLevel level : values()) {
            if (totalPoints >= level.threshold) {
                result = level;
            }
        }
        return result;
    }
}
