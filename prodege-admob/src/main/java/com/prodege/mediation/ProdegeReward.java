package com.prodege.mediation;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.rewarded.RewardItem;

import org.jetbrains.annotations.NotNull;

/**
 * A {@link ProdegeReward} used to map Prodege's to Google's reward.
 */
public class ProdegeReward implements RewardItem {

    @NonNull
    private final String currency;

    @NonNull
    private final Integer points;

    public ProdegeReward(@NonNull String currency, @NonNull Integer points) {
        this.currency = currency;
        this.points = points;
    }

    @NotNull
    @Override
    public String getType() {
        return currency;
    }

    @Override
    public int getAmount() {
        return points;
    }

}