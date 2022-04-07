# Pollfish Android AdMob Mediation Adapter

AdMob Mediation Adapter for Android apps looking to load and show Rewarded Surveys from Pollfish in the same waterfall with other Rewarded Ads.

> **Note:** A detailed step by step guide is provided on how to integrate can be found [here](https://www.pollfish.com/docs/android-admob-adapter) 

## Step 1: Add Pollfish AdMob Adapter to your project

Download the following libraries

* [Pollfish SDK](https://pollfish.com/docs/android/google-play)
* [PollfishAdMobAdapter](https://pollfish.com/docs/android-admob-adapter)

Import Pollfish AdMob Adapter and Pollfish SDK **.aar** libraries.  

If you are using Android Studio, right click on your project and select New Module. Then select Import .JAR or .AAR Package option and from the file browser locate Pollfish AdMob Adapter aar file. Right click again on your project and in the Module Dependencies tab choose to add Pollfish module that you recently added, as a dependency.

**OR**

#### **Retrieve Pollfish AdMob Adapter through maven()**

Retrieve Pollfish through **maven()** with gradle by adding the following line in your project **build.gradle** (not the top level one, the one under 'app') in  dependencies section:  

```
dependencies {
  implementation 'com.pollfish.mediation:pollfish-admob:6.2.3.0'
}
```

## Step 2: Request for a RewardedAd

Import the following packages

```java
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
```

<br/>

Initialize AdMob SDK by calling `MobileAds.initialize` method passing a `Context` and an `OnInitializationCompleteListener` listener as arguments.

<span style="text-decoration:underline">Kotlin</span>

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    ...

    MobileAds.initialize(this) {
        // Initialization completed
        createAndLoadRewardedAd()
    }
}
```

<span style="text-decoration:underline">Java</span>

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    ...

    MobileAds.initialize(this, initializationStatus -> {
        // Initialization completed
        createAndLoadRewardedAd();
    });
}
```

Request a Rewarded A from AdMob by calling `RewardedAd.load` passing a `Context`, you `AD_UNIT_KEY`, an `AdRequest` object instance and a `RewardedAdLoadCallback` abstract class implementation as arguments.

<span style="text-decoration:underline">Kotlin</span>

```kotlin
val adRequest = AdRequest.Builder().build()

RewardedAd.load(this, "AD_UNIT_KEY", adRequest, object : RewardedAdLoadCallback() {
    override fun onAdLoaded(rewardedAd: RewardedAd) {
        
        rewardedAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {}

            override fun onAdShowedFullScreenContent() {}

            override fun onAdDismissedFullScreenContent() {}
        }
    }

    override fun onAdFailedToLoad(loadAdError: LoadAdError) {}
})
```

<span style="text-decoration:underline">Java</span>

```java
AdRequest request = new AdRequest.Builder().build();

RewardedAd.load(this, "AD_UNIT_KEY", request, new RewardedAdLoadCallback() {

    @Override
    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {}

            @Override
            public void onAdShowedFullScreenContent() {}

            @Override
            public void onAdDismissedFullScreenContent() {}
        });
    }

    @Override
    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {}

});
```

When the Rewarded Ad is ready, present the ad by invoking `rewardedAd.show` passing an `Activity` and a reward completion block. Just to be sure, you can combine show with a check to see if the Ad you are about to show is actually ready.

<span style="text-decoration:underline">Kotlin</span>

```kotlin
mRewardedAd?.show(this) { reward: RewardItem ->
    // Reward received
}
```

<span style="text-decoration:underline">Java</span>

```java
if (mRewardedAd != null) {
    mRewardedAd.show(MainActivity.this, reward -> {
        // Reward received
    });
}
```


## Step 3: Configure Pollfish AdMob Adapter in your Rewarded Ad Unit 

Pollfish AdMob Adapter provides different options that you can use to control the behaviour of Pollfish SDK.

<br/>

Below you can see all the available options of **PollfishExtrasBundleBuilder** instance that is used to configure the behaviour of Pollfish SDK.

<br/>

No | Description
------------ | -------------
3.1 | **`.setAPIKey(String apiKey)`**  <br/> Sets Pollfish SDK API key as provided by Pollfish
3.2 | **`.setRequestUUID(String requestUUID)`**  <br/> Sets a unique id to identify a user and be passed through server-to-server callbacks
3.3 | **`.setReleaseMode(boolean releaseMode)`**  <br/> Sets Pollfish SDK to Developer or Release mode
3.4 | **`.setOfferwallMode(boolean offerwallMode)`** <br/> Sets Pollfish SDK to Offerwall Mode

#### 2.1 .setAPIKey(String apiKey)

Pollfish API Key as provided by Pollfish on  [Pollfish Dashboard](https://www.pollfish.com/publisher/) after you sign up to the platform.  If you have already specified Pollfish API Key on AdMob's UI, this param will be ignored.

#### 2.2 .setRequestUUID(String requestUUID)

Sets a unique id to identify a user and be passed through server-to-server callbacks on survey completion. 

In order to register for such callbacks you can set up your server URL on your app's page on Pollfish Developer Dashboard and then pass your requestUUID through ParamsBuilder object during initialization. On each survey completion you will receive a callback to your server including the requestUUID param passed.

If you would like to read more on Pollfish s2s callbacks you can read the documentation [here](https://www.pollfish.com/docs/s2s)

#### 2.3 .setReleaseMode(boolean releaseMode)

Sets Pollfish SDK to Developer or Release mode.

*   **Developer mode** is used to show to the developer how Pollfish surveys will be shown through an app (useful during development and testing).
*   **Release mode** is the mode to be used for a released app in any app store (start receiving paid surveys).

Pollfish AdMob Adapter runs Pollfish SDK in release mode by default. If you would like to test with Test survey, you should set release mode to fasle.

<span style="text-decoration:underline">Kotlin</span>

```kotlin
val bundle = PollfishExtrasBundleBuilder()
    .setAPIKey(POLLFISH_API_KEY)
    .setReleaseMode(false)
    .setRequestUUID("MY_UUID")
    .build()

val request = AdRequest.Builder()
    .addNetworkExtrasBundle(PollfishAdMobAdapter::class.java, bundle)
    .build()
```

<span style="text-decoration:underline">Java</span>

```java
Bundle pollfishBundle = new PollfishExtrasBundleBuilder()
    .setAPIKey("YOUR_POLLFISH_API_KEY")
    .setReleaseMode(false)
    .setRequestUUID("MY_ID")
    .build();

AdRequest request = new AdRequest.Builder()
    .addNetworkExtrasBundle(PollfishAdMobAdapter.class, pollfishBundle)
    .build();
```

### Step 3: Publish 

If you everything worked fine during the previous steps, you should turn Pollfish to release mode and publish your app.

> **Note:** After you take your app live, you should request your account to get verified through Pollfish Dashboard in the App Settings area.

> **Note:** There is an option to show **Standalone Demographic Questions** needed for Pollfish to target users with surveys even when no actually surveys are available. Those surveys do not deliver any revenue to the publisher (but they can increase fill rate) and therefore if you do not want to show such surveys in the Waterfall you should visit your **App Settings** are and disable that option.

