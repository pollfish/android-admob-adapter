package com.prodege.mediation;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

public class ProdegeInitializationConfiguration {
    @NonNull
    final String apiKey;
    @Nullable
    final String userId;
    @Nullable
    final Boolean testMode;

    public ProdegeInitializationConfiguration(@NonNull String apiKey, @Nullable String userId, @Nullable Boolean testMode) {
        this.apiKey = apiKey;
        this.userId = userId;
        this.testMode = testMode;
    }

    @Nullable
    public static ProdegeInitializationConfiguration fromBundle(@NonNull Bundle bundle, @Nullable Boolean localTestMode, @Nullable String localUserId, @Nullable String localApiKey) {
        String remoteJsonPayload = bundle.getString(ProdegeConstants.KEY_REMOTE_PARAMS);

        String remoteApiKey = null;
        Boolean remoteTestMode = null;

        if (remoteJsonPayload != null) {
            try {
                JSONObject jsonObject = new JSONObject(remoteJsonPayload);

                if (jsonObject.has((ProdegeConstants.KEY_API_KEY)) && !TextUtils.isEmpty(jsonObject.getString(ProdegeConstants.KEY_API_KEY))) {
                    remoteApiKey = jsonObject.getString(ProdegeConstants.KEY_API_KEY);
                }

                if (jsonObject.has(ProdegeConstants.KEY_TEST_MODE)) {
                    remoteTestMode = jsonObject.getBoolean(ProdegeConstants.KEY_TEST_MODE);
                }
            } catch (Throwable t) {
                String logMessage = String
                        .format("Could not parse malformed JSON:  %s, will resolve local configuration.", remoteJsonPayload);
                Log.w(ProdegeConstants.TAG, logMessage);
            }
        } else {
            Log.w(ProdegeConstants.TAG, "Could not retrieve remote adapter configuration, will resolve local configuration.");
        }

        Boolean testMode = localTestMode != null ? localTestMode : remoteTestMode;
        String apiKey = !TextUtils.isEmpty(localApiKey) ? localApiKey : remoteApiKey;
        String userId = !TextUtils.isEmpty(localUserId) ? localUserId : null;

        if (apiKey == null) {
            Log.e(ProdegeConstants.TAG, "API key is not defined. Please review your integration.");
            return null;
        }

        return new ProdegeInitializationConfiguration(apiKey, userId, testMode);
    }
}
