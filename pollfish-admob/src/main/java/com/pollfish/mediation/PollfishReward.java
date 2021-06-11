package com.pollfish.mediation;

import com.google.android.gms.ads.rewarded.RewardItem;

import org.jetbrains.annotations.NotNull;

/**
 * A {@link PollfishReward} used to map Pollfish reward to Google's reward.
 */
public class PollfishReward implements RewardItem {

    private final String rewardName;
    private final int rewardValue;

    public PollfishReward(String rewardName, int rewardValue) {
        this.rewardName = rewardName;
        this.rewardValue = rewardValue;
    }

    @NotNull
    @Override
    public String getType() {
        return rewardName;
    }

    @Override
    public int getAmount() {
        return rewardValue;
    }

}