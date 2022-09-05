package net.pubnative.easysound.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.multidex.MultiDex;
import androidx.viewpager.widget.ViewPager;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import net.pubnative.easysound.R;
import net.pubnative.easysound.fragments.FileViewerFragment;
import net.pubnative.easysound.fragments.RecordFragment;
import net.pubnative.lite.sdk.HyBid;
import net.pubnative.lite.sdk.VideoListener;
import net.pubnative.lite.sdk.interstitial.HyBidInterstitialAd;

public class MainActivity extends AppCompatActivity implements HyBidInterstitialAd.Listener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final int POSITION_RECORD = 0;
    private static final int POSITION_LIST = 1;
    private static final int REQUEST_PERMISSIONS = 100;

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

        if (mRecordFragment == null) {
            mRecordFragment = RecordFragment.newInstance(POSITION_RECORD);
        }
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
    public void onBackPressed() {
        mRecordFragment.pauseOnBackPressed();
        super.onBackPressed();
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
        askPermissions();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
        }
        else {
            return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    public void askPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                checkPermissionsForQ();
            }
        }
        else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE
                }, REQUEST_PERMISSIONS);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public void checkPermissionsForQ() {
        requestPermissions(new String[]
                        {Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                REQUEST_PERMISSIONS);
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
        Log.d(LOG_TAG, "onInterstitialLoadFailed");
    }

    @Override
    public void onInterstitialImpression() {
        Log.d(LOG_TAG, "onInterstitialImpression");
    }

    @Override
    public void onInterstitialDismissed() {
        Log.d(LOG_TAG, "onInterstitialDismissed");
    }

    @Override
    public void onInterstitialClick() {
        Log.d(LOG_TAG, "onInterstitialClick");
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
}
