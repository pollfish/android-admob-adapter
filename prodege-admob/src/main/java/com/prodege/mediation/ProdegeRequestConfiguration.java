package com.prodege.mediation;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

class ProdegeRequestConfiguration {
    @NonNull
    final String placementId;
    @Nullable
    final String requestUuid;
    @Nullable
    final Integer surveyFormat;
    @Nullable
    final Boolean muted;

    public ProdegeRequestConfiguration(@NonNull String placementId, @Nullable String requestUuid, @Nullable Boolean muted, @Nullable Integer surveyFormat) {
        this.placementId = placementId;
        this.requestUuid = requestUuid;
        this.muted = muted;
        this.surveyFormat = surveyFormat;
    }

    @Nullable
    public static ProdegeRequestConfiguration fromBundle(@NonNull Bundle serverExtras, @NonNull Bundle networkExtras) {
        String remoteJsonPayload = serverExtras.getString(ProdegeConstants.KEY_REMOTE_PARAMS);

        String placementId = null;
        String requestUuid = null;
        Boolean muted = null;
        Integer surveyFormat = null;

        if (remoteJsonPayload != null) {
            try {
                JSONObject jsonObject = new JSONObject(remoteJsonPayload);

                if (jsonObject.has(ProdegeConstants.KEY_PLACEMENT_ID) && !TextUtils.isEmpty(jsonObject.getString(ProdegeConstants.KEY_PLACEMENT_ID))) {
                    placementId = jsonObject.getString(ProdegeConstants.KEY_PLACEMENT_ID);
                }

                if (jsonObject.has(ProdegeConstants.KEY_REQUEST_UUID) && !TextUtils.isEmpty(jsonObject.getString(ProdegeConstants.KEY_REQUEST_UUID))) {
                    requestUuid = jsonObject.getString(ProdegeConstants.KEY_REQUEST_UUID);
                }

                if (jsonObject.has(ProdegeConstants.KEY_MUTED)) {
                    muted = jsonObject.getBoolean(ProdegeConstants.KEY_MUTED);
                }

                if (jsonObject.has(ProdegeConstants.KEY_SURVEY_FORMAT)) {
                    surveyFormat = jsonObject.getInt(ProdegeConstants.KEY_SURVEY_FORMAT);
                }
            } catch (Exception e) {
                String logMessage = String
                        .format("Could not parse malformed JSON: %s, will resolve local configuration.", remoteJsonPayload);
                Log.w(ProdegeConstants.TAG, logMessage);
            }
        } else {
            Log.w(ProdegeConstants.TAG, "Could not retrieve remote adapter configuration, will resolve local configuration.");
        }

        if (networkExtras.containsKey(ProdegeConstants.KEY_PLACEMENT_ID) && !TextUtils.isEmpty(ProdegeConstants.KEY_PLACEMENT_ID)) {
            placementId = networkExtras.getString(ProdegeConstants.KEY_PLACEMENT_ID);
        }

        if (networkExtras.containsKey(ProdegeConstants.KEY_REQUEST_UUID) && !TextUtils.isEmpty(ProdegeConstants.KEY_REQUEST_UUID)) {
            requestUuid = networkExtras.getString(ProdegeConstants.KEY_REQUEST_UUID);
        }

        if (networkExtras.containsKey(ProdegeConstants.KEY_MUTED)) {
            muted = networkExtras.getBoolean(ProdegeConstants.KEY_MUTED);
        }

        if (networkExtras.containsKey(ProdegeConstants.KEY_SURVEY_FORMAT)) {
            surveyFormat = networkExtras.getInt(ProdegeConstants.KEY_SURVEY_FORMAT);
        }

        if (TextUtils.isEmpty(placementId)) {
            Log.e(ProdegeConstants.TAG, "Placement Id is not defined. Please review your integration.");
            return null;
        }

        return new ProdegeRequestConfiguration(placementId, requestUuid, muted, surveyFormat);
    }

}