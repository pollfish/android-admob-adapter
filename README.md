# Prodege Android AdMob Mediation Adapter

AdMob Mediation Adapter for Android apps looking to load and show Rewarded ADS from Prodege in the same waterfall with other Rewarded Ads.

> **Note:** A detailed step by step guide is provided on how to integrate can be found [here](https://www.pollfish.com/docs/android-admob-adapter) 

## Add Prodege AdMob Adapter to your project

Retrieve Prodege AdMob Adapter through **maven()** with gradle by adding the following line in your app's module **build.gradle** file:

```groovy
dependencies {
  implementation 'com.prodege.mediation:prodege-admob:7.0.0-beta05.0'
}
```

<br/>

## Request for a RewardedAd

Import the following packages

<span style="text-decoration:underline">Kotlin</span>

```kotlin
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
```

<span style="text-decoration:underline">Java</span>

```java
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
```

Initialize AdMob SDK by calling `MobileAds.initialize` method passing a `Context` and an `OnInitializationCompleteListener` listener as arguments.

<span style="text-decoration:underline">Kotlin</span>

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // ...

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
    // ...

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

<br/>

## Configure the Prodege SDK programmatically

Prodege AdMob Adapter provides different options that you can use to control the behaviour of Prodege SDK. Any configuration, if applied, will override any configuration done in AdMob's dashboard.

### **Initialization configuration**

Below you can see all the availbale options for configuring Prodege SDK prior to the AdMob SDK initialization.

### `.setUserId(String)`

An optional id used to identify a user.

Setting the Prodege's `userId` will override the default behaviour and use that instead of the Advertising Id in order to identify a user.

> **Note:** <span style="color: red">You can pass the id of a user as identified on your system. Prodege will use this id to identify the user across sessions instead of an ad id/idfa as advised by the stores. You are solely responsible for aligning with store regulations by providing this id and getting relevant consent by the user when necessary. Prodege takes no responsibility for the usage of this id. In any request from your users on resetting/deleting this id and/or profile created, you should be solely liable for those requests.</span>
d
<span style="text-decoration:underline">Kotlin</span>

```kotlin
ProdegeAdMobAdapter.setUserId("MY_USER_ID")
```

<span style="text-decoration:underline">Java</span>

```java
ProdegeAdMobAdapter.setUserId("MY_USER_ID");
```

<br/>

### `.setTestMode(Boolean)`

Toggles the Prodege SDK Test mode.

- **`true`** is used to show to the developer how Prodege ads will be shown through an app (useful during development and testing).
- **`false`** is the mode to be used for a released app in any app store (start receiving paid surveys).

If you have already specified a placement id on AdMob's UI, this will override the one defined on Web UI.

Prodege AdMob Adapter works by default in live mode. If you would like to test with test ads:

<span style="text-decoration:underline">Kotlin</span>

```kotlin
ProdegeAdMobAdapter.setTestMode(true)
```

<span style="text-decoration:underline">Java</span>

```java
ProdegeAdMobAdapter.setTestMode(true);
```

<br/>

### `.setApiKey(String)`

Your application's API key as provided by the [Publisher Dashboard](https://www.pollfish.com/publisher/).

If you have already specified a placement id on AdMob's UI, this will override the one defined on Web UI.

<span style="text-decoration:underline">Kotlin</span>

```kotlin
ProdegeAdMobAdapter.setApiKey("PRODEGE_API_KEY")
```

<span style="text-decoration:underline">Java</span>

```java
ProdegeAdMobAdapter.setApiKey("PRODEGE_API_KEY");
```

<br/>

After configuring the Prodege Max Adapter you can proceed with the AdMob SDK initialization.

<span style="text-decoration:underline">Kotlin</span>

```kotlin
MobileAds.initialize(this) {
    // ...
}
```

<span style="text-decoration:underline">Java</span>

```java
MobileAds.initialize(this, initializationStatus -> {
    // ...
});
```

<br/>

### **Ad Request configuration**

Below you can see all the availbale options of `ProdegeNetworkExtras.Builder` for configuring your Prodege placement loaded by the `AdRequest`.

Start by creating a `ProdegeNetworkExtras.Builder` instance. You can later build upon this instnace based on you preffered configuration.

<span style="text-decoration:underline">Kotlin</span>

```kotlin
val builder = ProdegeNetworkExtras.Builder()
```

<span style="text-decoration:underline">Java</span>

```java
ProdegeNetworkExtras.Builder builder = new ProdegeNetworkExtras.Builder();
```

<br/>

### `.placementId(String)`

Your ad unit's placement id as provided by [Publisher Dashboard](https://www.pollfish.com/publisher/).

If you have already specified a placement id on AdMob's UI, this param will override the one defined on Web UI.

<span style="text-decoration:underline">Kotlin</span>

```kotlin
builder.placementId("PLACEMENT_ID")
```

<span style="text-decoration:underline">Java</span>

```java
builder.placementId("PLACEMENT_ID");
```

<br/>

### `.requestUuid(String)`

Sets a pass-through param to be received via the server-to-server callbacks.

In order to register for such callbacks you can set up your server URL on your app's page on the [Publisher Dashboard](https://www.pollfish.com/publisher/). On each survey completion you will receive a callback to your server including the `request_uuid` param passed.

If you would like to read more on Prodege s2s callbacks you can read the documentation [here](https://www.pollfish.com/docs/s2s)

If you have already specified a placement id on AdMob's UI, this param will override the one defined on Web UI.

<span style="text-decoration:underline">Kotlin</span>

```kotlin
builder.requestUuid("MY_REQUEST_UUID")
```

<span style="text-decoration:underline">Java</span>

```java
builder.requestUuid("MY_REQUEST_UUID");
```

<br/>

### `.muted(Boolean)`

Sets Prodege video ads mute state.

If you would like to read more on Prodege s2s callbacks you can read the documentation [here](https://www.pollfish.com/docs/s2s).

<span style="text-decoration:underline">Kotlin</span>

```kotlin
builder.muted(true)
```

<span style="text-decoration:underline">Java</span>

```java
builder.muted(true);
```

<br/>

Finally, build the `ProdegeNetworkExtras` instance, by calling `builder.build()` method on the `ProdegeNetworkExtras.Builder` instance.

<span style="text-decoration:underline">Kotlin</span>

```kotlin
val networkExtras = builder.build()

val request = AdRequest.Builder()
    .addNetworkExtrasBundle(ProdegeAdMobAdapter::class.java, networkExtras)
    .build()
```

<span style="text-decoration:underline">Java</span>

```java
Bundle networkExtras = builder.build();

AdRequest request = new AdRequest.Builder()
    .addNetworkExtrasBundle(ProdegeAdMobAdapter.class, networkExtras)
    .build();
```

<br/>

### Publish 

If everything worked fine during the previous steps, you are ready to proceed with publishing your app.

> **Note:** After you take your app live, you should request your account to get verified through Prodege Dashboard in the App Settings area.

> **Note:** There is an option to show **Standalone Demographic Questions** needed for Prodege to target users with surveys even when no actually surveys are available. Those surveys do not deliver any revenue to the publisher (but they can increase fill rate) and therefore if you do not want to show such surveys in the Waterfall you should visit your **App Settings** are and disable that option. You can read more [here](https://www.pollfish.com/docs/demographic-surveys)

<br/>

# More info

You can read more info on how the Prodege SDKs work or how to get started with Google AdMob at the following links:

[Prodege Android SDK](https://pollfish.com/docs/android-v7)

[AdMob Android SDK](https://developers.google.com/admob/android/quick-start)