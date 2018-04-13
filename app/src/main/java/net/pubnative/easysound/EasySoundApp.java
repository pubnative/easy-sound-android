package net.pubnative.easysound;

import android.app.Application;

import net.pubnative.lite.sdk.PNLite;

public class EasySoundApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PNLite.initialize("e7621bc9cc5649a9b72693faef56d5d7", this);
    }
}
