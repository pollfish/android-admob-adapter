package com.prodege.mediation;

import com.google.android.gms.ads.AdError;
import com.prodege.listener.ProdegeException;

final class ProdegeAdMobAdapterUtils {

    public static AdError toAdError(ProdegeException exception) {
        int adapterError;

        if (exception instanceof ProdegeException.ConnectionError) {
            adapterError = ProdegeAdMobAdapter.ERROR_NO_CONNECTION;
        } else if (exception instanceof ProdegeException.EmptyApiKey || exception instanceof ProdegeException.WrongApiKey || exception instanceof ProdegeException.EmptyPlacementId || exception instanceof ProdegeException.WrongPlacementId) {
            adapterError = ProdegeAdMobAdapter.ERROR_INVALID_CONFIGURATION;
        } else if (exception instanceof ProdegeException.NoFill) {
            adapterError = ProdegeAdMobAdapter.ERROR_NO_FILL;
        } else if (exception instanceof ProdegeException.NotInitialized) {
            adapterError = ProdegeAdMobAdapter.ERROR_NOT_INITIALIZED;
        } else {
            adapterError = ProdegeAdMobAdapter.ERROR_INTERNAL;
        }

        return new AdError(adapterError, exception.getMessage(), ProdegeConstants.DOMAIN);
    }
}
