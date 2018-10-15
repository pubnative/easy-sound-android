package net.pubnative.easysound;

import android.app.Application;

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.SdkInitializationListener;

import net.pubnative.lite.sdk.HyBid;
import net.pubnative.lite.sdk.PNLite;

public class EasySoundApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PNLite.initialize("e7621bc9cc5649a9b72693faef56d5d7", this, new HyBid.InitialisationListener() {
            @Override
            public void onInitialisationFinished(boolean success) {
                SdkConfiguration sdkConfiguration = new SdkConfiguration
                        .Builder(getString(R.string.mopub_banner_ad_unit_id))
                        .build();
                MoPub.initializeSdk(EasySoundApp.this, sdkConfiguration, new SdkInitializationListener() {
                    @Override
                    public void onInitializationFinished() {

                    }
                });
            }
        });
    }
}
