package com.prodege.mediation;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.prodege.Prodege;
import com.prodege.builder.AdOptions;
import com.prodege.listener.ProdegeException;
import com.prodege.listener.ProdegeShowListener;

import org.jetbrains.annotations.NotNull;

public class ProdegeMediationRewardedAd implements MediationRewardedAd {

    @Nullable
    private MediationRewardedAdCallback mMediationRewardedAdCallback = null;

    @NonNull
    private final ProdegeRequestConfiguration configuration;

    public ProdegeMediationRewardedAd(@NonNull ProdegeRequestConfiguration configuration) {
        this.configuration = configuration;
    }

    public void setMediationRewardedAdCallback(@NonNull MediationRewardedAdCallback mediationRewardedAdCallback) {
        mMediationRewardedAdCallback = mediationRewardedAdCallback;
    }

    @Override
    public void showAd(@NotNull Context context) {
        if (Build.VERSION.SDK_INT < 21) {
            if (mMediationRewardedAdCallback != null) {
                mMediationRewardedAdCallback.onAdFailedToShow(
                        new AdError(ProdegeAdMobAdapter.ERROR_LOW_TARGET,
                                "Prodege SDK will not run on targets lower than 21.", ProdegeConstants.DOMAIN));
            }
            return;
        }

        AdOptions.Builder adOptionsBuilder = new AdOptions.Builder();

        if (configuration.muted != null) {
            adOptionsBuilder.muted(configuration.muted);
        }

        Prodege.showPlacement(configuration.placementId, new ProdegeShowListener() {
            @Override
            public void onOpened(@NonNull String s) {
                if (mMediationRewardedAdCallback != null) {
                    mMediationRewardedAdCallback.onAdOpened();
                }
            }

            @Override
            public void onClosed(@NonNull String s) {
                if (mMediationRewardedAdCallback != null) {
                    mMediationRewardedAdCallback.onAdClosed();
                }
            }

            @Override
            public void onShowFailed(@NonNull String s, @NonNull ProdegeException e) {
                if (mMediationRewardedAdCallback != null) {
                    mMediationRewardedAdCallback.onAdFailedToShow(ProdegeAdMobAdapterUtils.toAdError(e));
                }
            }
        }, adOptionsBuilder.build());
    }
}
