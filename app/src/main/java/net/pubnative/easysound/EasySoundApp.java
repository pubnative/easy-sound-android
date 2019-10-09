package net.pubnative.easysound;

import android.app.Application;

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;

import net.pubnative.lite.sdk.HyBid;

public class EasySoundApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        /*HyBid.initialize("dde3c298b47648459f8ada4a982fa92d", this, success -> {
            SdkConfiguration sdkConfiguration = new SdkConfiguration
                    .Builder(getString(R.string.mopub_banner_ad_unit_id))
                    .build();
            MoPub.initializeSdk(EasySoundApp.this, sdkConfiguration, () -> {

            });
        });*/
    }
}
