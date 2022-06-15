package com.pollfish.mediation;

import android.os.Bundle;


/**
 * The {@link PollfishExtrasBundleBuilder} class is used to create a mediation extras bundle
 * that can be passed to the adapter as extra data to be used in making requests. In this
 * example the sample SDK has two extra parameters that it can use to customize its ad requests.
 */
public class PollfishExtrasBundleBuilder {

    private final Bundle extras;

    public PollfishExtrasBundleBuilder() {
        this.extras = new Bundle();
    }

    public PollfishExtrasBundleBuilder setAPIKey(String apiKey) {
        extras.putString(PollfishAdMobAdapterConstants.POLLFISH_API_KEY, apiKey);
        return PollfishExtrasBundleBuilder.this;
    }

    public PollfishExtrasBundleBuilder setReleaseMode(boolean releaseMode) {
        extras.putBoolean(PollfishAdMobAdapterConstants.POLLFISH_MODE, releaseMode);
        return PollfishExtrasBundleBuilder.this;
    }

    public PollfishExtrasBundleBuilder setOfferwallMode(boolean offerwallMode) {
        extras.putBoolean(PollfishAdMobAdapterConstants.POLLFISH_INTEGRATION_TYPE, offerwallMode);
        return PollfishExtrasBundleBuilder.this;
    }

    public PollfishExtrasBundleBuilder setRequestUUID(String requestUUID) {
        extras.putString(PollfishAdMobAdapterConstants.POLLFISH_REQUEST_UUID, requestUUID);
        return PollfishExtrasBundleBuilder.this;
    }

    public Bundle build() {
        return extras;
    }
}