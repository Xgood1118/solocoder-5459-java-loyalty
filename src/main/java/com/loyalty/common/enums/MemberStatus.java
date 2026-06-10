package com.loyalty.common.enums;

import lombok.Getter;

@Getter
public enum MemberStatus {
    ACTIVE(1, "正常"),
    FROZEN(2, "违规冻结"),
    CANCELLING(3, "注销中");

    private final int code;
    private final String name;

    MemberStatus(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static MemberStatus fromCode(int code) {
        for (MemberStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return ACTIVE;
    }

    public boolean canConsume() {
        return this == ACTIVE;
    }
}
