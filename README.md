# Pollfish Android AdMob Mediation Adapter

AdMob mediation adapter for android apps looking to load and show Rewarded Surveys from Pollfish in the same waterfall with other Rewarded Ads.

### Step 1: Add Pollfish AdMob Adapter to your project

Import Pollfish AdMob adater **.AAR** file to your project libraries  

If you are using Android Studio, right click on your project and select New Module. Then select Import .JAR or .AAR Package option and from the file browser locate Pollfish aar file. Right click again on your project and in the Module Dependencies tab choose to add Pollfish module that you recently added, as a dependency.

**OR**

#### **Retrieve Pollfish AdMob Adapter through jCenter()**

Retrieve Pollfish through **jCenter()** with gradle by adding the following line in your project **build.gradle** (not the top level one, the one under 'app') in  dependencies section:  

```
dependencies {
  implementation 'com.pollfish.mediation:pollfish-admob:5.0.2.1'
}
```

### Step 2: Use and control Pollfish AdMob Adapter in your Rewarded Ad Unit 

Pollfish AdMob Adapter provides different options that you can use to control the behaviour of Pollfish SDK.

<br/>
Below you can see all the available options of **PollfishExtrasBundleBuilder** instance that is used to configure the behaviour of Pollfish SDK.
<br/>

No | Description
------------ | -------------
5.1 | **.setAPIKey(String apiKey)**  <br/> Sets Pollfish SDK API key as provided on Pollfish
5.2 | **.setRequestUUID(String requestUUID)**  <br/> Sets a unique id to identify a user and be passed through server-to-server callbacks
5.3 | **.setReleaseMode(boolean releaseMode)**  <br/> Sets Pollfish SDK to Developer or Release mode


<br/>
### 2.1 .setAPIKey(String apiKey)

Pollfish API Key as provided by Pollfish on  Pollfish Dashboard. If you did not specify the API Key in AdMob's UI as desribed in step 2. If you have already specified Pollfish API Key on AdMob's UI, this param will be ignored.

#### 2.2 .setRequestUUID(String requestUUID)

Sets a unique id to identify a user and be passed through server-to-server callbacks on survey completion. 

In order to register for such callbacks you can set up your server URL on your app's page on Pollfish Developer Dashboard and then pass your requestUUID through ParamsBuilder object during initialization. On each survey completion you will receive a callback to your server including the requestUUID param passed.

If you would like to read more on Pollfish s2s cllab

#### 2.3 .setReleaseMode(boolean releaseMode)

Sets Pollfish SDK to Developer or Release mode.

*   **Developer mode** is used to show to the developer how Pollfish surveys will be shown through an app (useful during development and testing).
*   **Release mode** is the mode to be used for a released app in any app store (start receiving paid surveys).

Pollfish AdMob Adapter runs Pollfish SDK in release mode by default. If you would like to test with Test survey, you should set release mode to fasle.
```
Bundle pollfishBundle = new PollfishExtrasBundleBuilder()
    .setAPIKey(""YOUR_POLLFISH_API_KEY")
    .setReleaseMode(false)
    .setRequestUUID("MY_ID")
    .build();

AdRequest request = new AdRequest.Builder()
                .addTestDevice("xxxxx-xxxx-xxxxxxx")
                .addNetworkExtrasBundle(PollfishAdMobAdapter.class, pollfishBundle)
                .build();
```

#### Step 3: Publish 

If you everything worked fine during the previous steps, you should turn Pollfish to release mode and publish your app.

A detailed step by step guide is provided on how to integrate can be found [here](https://github.com/pollfish/docs/edit/master/android-admob-adapter.md) 
