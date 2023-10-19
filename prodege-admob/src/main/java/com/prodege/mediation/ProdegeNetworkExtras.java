package com.prodege.mediation;

import android.os.Bundle;

import androidx.annotation.NonNull;


/**
 * The {@link ProdegeNetworkExtras} class is used to create a mediation extras bundle
 * that can be passed to the adapter as extra data to be used in making requests. In this
 * example the sample SDK has two extra parameters that it can use to customize its ad requests.
 */
public class ProdegeNetworkExtras {

    public static class Builder {
        private final Bundle extras;

        public Builder() {
            this.extras = new Bundle();
        }

        public Builder placementId(@NonNull String placementId) {
            extras.putString(ProdegeConstants.KEY_PLACEMENT_ID, placementId);
            return this;
        }

        public Builder muted(boolean muted) {
            extras.putBoolean(ProdegeConstants.KEY_MUTED, muted);
            return this;
        }

        public Builder requestUuid(@NonNull String requestUuid) {
            extras.putString(ProdegeConstants.KEY_REQUEST_UUID, requestUuid);
            return this;
        }

        public Bundle build() {
            return extras;
        }
    }

}