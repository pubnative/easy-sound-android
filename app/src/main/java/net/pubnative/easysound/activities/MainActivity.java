package net.pubnative.easysound.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.multidex.MultiDex;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;
import com.mopub.common.privacy.ConsentDialogListener;
import com.mopub.common.privacy.PersonalInfoManager;
import com.mopub.mobileads.MoPubErrorCode;

import net.pubnative.easysound.EasySoundApp;
import net.pubnative.easysound.R;
import net.pubnative.easysound.fragments.FileViewerFragment;
import net.pubnative.easysound.fragments.RecordFragment;
import net.pubnative.lite.sdk.HyBid;
import net.pubnative.lite.sdk.consent.UserConsentActivity;
import net.pubnative.lite.sdk.interstitial.HyBidInterstitialAd;

public class MainActivity extends AppCompatActivity implements HyBidInterstitialAd.Listener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int POSITION_RECORD = 0;
    private static final int POSITION_LIST = 1;
    private static final int REQUEST_PERMISSIONS = 100;
    private final static int REQUEST_CONSENT = 103;

    private final static String PREF_CONSENT = "pn_consent";
    private final static String PREF_CONSENT_GAID = "pn_consent_gaid";
    private final static String PREF_LAST_CONSENT_ASKED_DATE = "pn_consent_date";

    private boolean isActive = false;

    private ViewPager mPager;
    private SectionsPagerAdapter mPagerAdapter;
    private RecordFragment mRecordFragment;
    private FileViewerFragment mFilesFragment;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecordFragment = RecordFragment.newInstance(POSITION_RECORD);
        mFilesFragment = FileViewerFragment.newInstance(POSITION_LIST);

        mPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mPager = findViewById(R.id.container);
        mPager.setAdapter(mPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs);

        mPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout) {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == POSITION_LIST) {
                    mFilesFragment.loadAd();
                }
            }
        });
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mPager));

        checkPermissions();

        if (savedInstanceState == null) {
            HyBid.initialize("dde3c298b47648459f8ada4a982fa92d", MainActivity.this.getApplication(), success -> {
                SdkConfiguration sdkConfiguration = new SdkConfiguration
                        .Builder(getString(R.string.mopub_banner_ad_unit_id))
                        .build();
                MoPub.initializeSdk(MainActivity.this.getApplication(), sdkConfiguration, () -> {

                });
            });
        }
        loadInterstitial();
    }

    HyBidInterstitialAd testAd = null;

    private void loadInterstitial() {

        testAd = new HyBidInterstitialAd(this, "3", this);
        testAd.load();
    }

    @Override
    protected void onStart() {
        super.onStart();

        isActive = true;

       // new Handler(Looper.getMainLooper()).postDelayed(consentRunnable, 4000);
    }

    @Override
    protected void onStop() {
        super.onStop();

        isActive = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, REQUEST_PERMISSIONS);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                boolean allPermissions = grantResults.length > 0;
                for (int i = 0; i < grantResults.length && allPermissions; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        allPermissions = false;
                    }
                }

                if (!allPermissions) {
                    Snackbar.make(findViewById(R.id.main_content), R.string.permissions_denied, Snackbar.LENGTH_LONG).show();
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onInterstitialLoaded() {
        testAd.show();
    }

    @Override
    public void onInterstitialLoadFailed(Throwable throwable) {

    }

    @Override
    public void onInterstitialImpression() {

    }

    @Override
    public void onInterstitialDismissed() {

    }

    @Override
    public void onInterstitialClick() {

    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case POSITION_RECORD: {
                    return mRecordFragment;
                }
                case POSITION_LIST: {
                    return mFilesFragment;
                }
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CONSENT) {
            setConsent(resultCode == UserConsentActivity.RESULT_CONSENT_ACCEPTED);
            showMoPubConsent();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private final Runnable consentRunnable = new Runnable() {
        @Override
        public void run() {
            if (isActive) {
                if (shouldAskForConsent()) {
                    Intent intent = HyBid.getUserDataManager().getConsentScreenIntent(MainActivity.this);
                    startActivityForResult(intent, REQUEST_CONSENT);
                } else {
                    showMoPubConsent();
                }
            }
        }
    };

    private void setConsent(boolean consent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(PREF_CONSENT, consent);
        editor.putString(PREF_CONSENT_GAID, HyBid.getDeviceInfo().getAdvertisingId());
        editor.putLong(PREF_LAST_CONSENT_ASKED_DATE, System.currentTimeMillis());
        editor.apply();
    }

    public boolean shouldAskForConsent() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        if (!preferences.contains(PREF_CONSENT)) {
            return true;
        }

        boolean consentGiven = preferences.getBoolean(PREF_CONSENT, false);
        String consentGaid = preferences.getString(PREF_CONSENT_GAID, "");

        if (!consentGaid.equalsIgnoreCase(HyBid.getDeviceInfo().getAdvertisingId())) {
            return true;
        } else {
            if (consentGiven) {
                return false;
            } else {
                long currentDate = System.currentTimeMillis();
                long lastDate = preferences.getLong(PREF_LAST_CONSENT_ASKED_DATE, currentDate);

                long difference = currentDate - lastDate;
                int daysPassed = (int) (difference / (1000 * 60 * 60 * 24));

                return daysPassed >= 30;
            }
        }
    }

    private void showMoPubConsent() {
        final PersonalInfoManager infoManager = MoPub.getPersonalInformationManager();
        if (infoManager.shouldShowConsentDialog()) {
            infoManager.loadConsentDialog(new ConsentDialogListener() {
                @Override
                public void onConsentDialogLoaded() {
                    if (isActive) {
                        infoManager.showConsentDialog();
                    }
                }

                @Override
                public void onConsentDialogLoadFailed(@NonNull MoPubErrorCode moPubErrorCode) {

                }
            });
        }
    }


}
