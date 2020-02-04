package com.pollfish.mediation;

import android.os.Bundle;
import android.util.Log;


/**
 * The {@link PollfishExtrasBundleBuilder} class is used to create a mediation extras bundle
 * that can be passed to the adapter as extra data to be used in making requests. In this
 * example the sample SDK has two extra parameters that it can use to customize its ad requests.
 */
public class PollfishExtrasBundleBuilder {   // Keys to add and obtain the extra parameters from the bundle.

    private static final String TAG = "ExtrasBundleBuilder'";

    static final String POLLFISH_API_KEY = "api_key";
    static final String POLLFISH_MODE = "release_mode";
    static final String POLLFISH_REQUEST_UUID = "request_uuid";
    static final String POLLFISH_INTEGRATION_TYPE = "offerwall_mode";

    /**
     * An extra value used to populate the api key property of Pollfish SDK
     */
    private String apiKey;

    /**
     * An extra value used to populate the "release mode" property of Pollfish SDK
     */
    private boolean releaseMode;


    /**
     * An extra unique identification around the user
     */
    private String requestUUID;

    /**
     * An extra value used to populate the "offerwall mode" property of Pollfish SDK
     */
    private boolean offerwallMode;


    public PollfishExtrasBundleBuilder setAPIKey(
            String apiKey) {

        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "setAPIKey: " + apiKey);
        this.apiKey = apiKey;
        return PollfishExtrasBundleBuilder.this;
    }

    public PollfishExtrasBundleBuilder setReleaseMode(boolean releaseMode) {
        if (PollfishAdMobAdapterConstants.DEBUGMODE)  Log.d(TAG, "setReleaseMode: " + releaseMode);
        this.releaseMode = releaseMode;
        return PollfishExtrasBundleBuilder.this;
    }

    public PollfishExtrasBundleBuilder setOfferwallMode(boolean offerwallMode) {
        if (PollfishAdMobAdapterConstants.DEBUGMODE)  Log.d(TAG, "setOfferwallMode: " + offerwallMode);
        this.offerwallMode = offerwallMode;
        return PollfishExtrasBundleBuilder.this;
    }

    public PollfishExtrasBundleBuilder setRequestUUID(String requestUUID) {
        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "setRequestUUID: " + requestUUID);
        this.requestUUID = requestUUID;
        return PollfishExtrasBundleBuilder.this;
    }

    public Bundle build() {

        if (PollfishAdMobAdapterConstants.DEBUGMODE) Log.d(TAG, "build()");

        Bundle extras = new Bundle();

        extras.putString(POLLFISH_API_KEY, apiKey);
        extras.putBoolean(POLLFISH_MODE, releaseMode);
        extras.putString(POLLFISH_REQUEST_UUID, requestUUID);
        extras.putBoolean(POLLFISH_INTEGRATION_TYPE, offerwallMode);

        return extras;
    }
}