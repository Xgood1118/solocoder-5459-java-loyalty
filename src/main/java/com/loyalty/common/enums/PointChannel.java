package com.loyalty.common.enums;

import lombok.Getter;

@Getter
public enum PointChannel {
    CONSUMPTION("consumption", "消费"),
    SIGN_IN("sign_in", "每日签到"),
    PROFILE_COMPLETE("profile_complete", "完善资料"),
    REFEREE("referee", "推荐奖励"),
    REFERRED("referred", "被推荐奖励"),
    REVIEW("review", "商品评价"),
    ACTIVITY("activity", "活动赠送"),
    EXCHANGE_GOODS("exchange_goods", "兑换商品"),
    EXCHANGE_COUPON("exchange_coupon", "兑换优惠券"),
    LOTTERY("lottery", "抽奖消耗"),
    DONATION("donation", "公益捐赠"),
    TRANSFER_OUT("transfer_out", "积分转出"),
    TRANSFER_IN("transfer_in", "积分转入"),
    TRANSFER_FEE("transfer_fee", "转账手续费"),
    EXPIRE("expire", "过期清零"),
    SYSTEM("system", "系统调整");

    private final String code;
    private final String name;

    PointChannel(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public boolean isIncome() {
        return this == CONSUMPTION || this == SIGN_IN || this == PROFILE_COMPLETE
                || this == REFEREE || this == REFERRED || this == REVIEW
                || this == ACTIVITY || this == TRANSFER_IN || this == SYSTEM;
    }
}
