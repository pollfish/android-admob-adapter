package com.pollfish.mediation;


import com.google.android.gms.ads.rewarded.RewardItem;

/**
 * A {@link PollfishReward} used to map Pollfish reward to Google's reward.
 */
public class PollfishReward implements RewardItem {

    private String rewardName;
    private int rewardValue;

    public PollfishReward(String rewardName,int rewardValue){

        this.rewardName=rewardName;
        this.rewardValue=rewardValue;
    }

    @Override
    public String getType() {
        return rewardName;
    }

    @Override
    public int getAmount() {
        return rewardValue;
    }
}