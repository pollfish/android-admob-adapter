package com.pollfish.mediation;


public interface PollfishAdMobAdapterConstants {

    /*
     *The adapter versioning scheme for versioned adapters is <third-party SDK version>.<adapter patch version>.
     */
    String POLLFISH_ADAPTER_VERSION = "6.2.4.2";

    int ERROR_CODE_NOT_AVAILABLE = 0;
    int ERROR_CODE_LOW_TARGET = 1;
    int ERROR_CODE_PANEL_ALREADY_VISIBLE = 2;
    int ERROR_CODE_WRONG_CONTEXT = 3;
    int ERROR_CODE_EMPTY_API_KEY = 4;

    String POLLFISH_API_KEY = "api_key";
    String POLLFISH_MODE = "release_mode";
    String POLLFISH_REQUEST_UUID = "request_uuid";
    String POLLFISH_INTEGRATION_TYPE = "offerwall_mode";
}
