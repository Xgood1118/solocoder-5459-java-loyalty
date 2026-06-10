package com.loyalty.redemption.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonationOrg {

    private String orgId;

    private String name;

    private String description;

    private int totalDonatedPoints;

    private boolean enabled;
}
