package net.pubnative.easysound.fragments;

import android.os.Bundle;
import android.os.FileObserver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubView;

import net.pubnative.easysound.R;
import net.pubnative.easysound.adapters.FileViewerAdapter;
import net.pubnative.lite.sdk.api.BannerRequestManager;
import net.pubnative.lite.sdk.api.RequestManager;
import net.pubnative.lite.sdk.models.Ad;
import net.pubnative.lite.sdk.utils.PrebidUtils;

public class FileViewerFragment extends Fragment implements MoPubView.BannerAdListener {
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = "FileViewerFragment";

    private int position;
    private FileViewerAdapter mFileViewerAdapter;
    private MoPubView mBannerView;

    public static FileViewerFragment newInstance(int position) {
        FileViewerFragment f = new FileViewerFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        observer.startWatching();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_file_viewer, container, false);

        RecyclerView mRecyclerView = v.findViewById(R.id.recyclerView);
        mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        //newest to oldest order (database stores from oldest to newest)
        llm.setReverseLayout(true);
        llm.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mFileViewerAdapter = new FileViewerAdapter(getActivity(), llm);
        mRecyclerView.setAdapter(mFileViewerAdapter);

        mBannerView = v.findViewById(R.id.banner_container);
        mBannerView.setAutorefreshEnabled(false);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBannerView.setBannerAdListener(this);
        mBannerView.setAutorefreshEnabled(false);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void loadAd() {
        RequestManager requestManager = new BannerRequestManager();
        requestManager.setZoneId("2");
        requestManager.setRequestListener(new RequestManager.RequestListener() {
            @Override
            public void onRequestSuccess(Ad ad) {
                if (getContext() != null && isResumed()) {
                    mBannerView.setAdUnitId("e7647b345b4646d3b07d57b4366386b4");
                    mBannerView.setKeywords(PrebidUtils.getPrebidKeywords(ad, "2"));
                    mBannerView.loadAd();
                }
            }

            @Override
            public void onRequestFail(Throwable throwable) {
                if (getContext() != null && isResumed()) {
                    mBannerView.setAdUnitId("e7647b345b4646d3b07d57b4366386b4");
                    mBannerView.loadAd();
                }
                Log.e(LOG_TAG, throwable.getMessage());
            }
        });
        requestManager.requestAd();
    }

    @Override
    public void onBannerLoaded(MoPubView banner) {
        if (getContext() != null && isResumed()) {
            mBannerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
        Log.e(LOG_TAG, errorCode.toString());
    }

    @Override
    public void onBannerClicked(MoPubView banner) {
        if (getContext() != null && isResumed()) {
            mBannerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBannerExpanded(MoPubView banner) { }

    @Override
    public void onBannerCollapsed(MoPubView banner) { }

    FileObserver observer =
            new FileObserver(android.os.Environment.getExternalStorageDirectory().toString()
                    + "/EasySound") {
                // set up a file observer to watch this directory on sd card
                @Override
                public void onEvent(int event, String file) {
                    if(event == FileObserver.DELETE){
                        // user deletes a recording file out of the app

                        String filePath = android.os.Environment.getExternalStorageDirectory().toString()
                                + "/EasySound" + file + "]";

                        Log.d(LOG_TAG, "File deleted ["
                                + android.os.Environment.getExternalStorageDirectory().toString()
                                + "/EasySound" + file + "]");

                        // remove file from database and recyclerview
                        mFileViewerAdapter.removeOutOfApp(filePath);
                    }
                }
            };
}




