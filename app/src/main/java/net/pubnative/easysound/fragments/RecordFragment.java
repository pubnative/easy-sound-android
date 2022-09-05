package net.pubnative.easysound.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.pubnative.easysound.R;
import net.pubnative.easysound.RecordingService;
import net.pubnative.easysound.activities.MainActivity;
import net.pubnative.lite.sdk.VideoListener;
import net.pubnative.lite.sdk.interstitial.HyBidInterstitialAd;

import java.io.File;

public class RecordFragment extends Fragment implements HyBidInterstitialAd.Listener, VideoListener {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_POSITION = "position";
    private static final String LOG_TAG = RecordFragment.class.getSimpleName();

    private static final int DELAY_RECORD_BUTTON = 1000;

    private int position;

    //Recording controls
    private FloatingActionButton mRecordButton = null;
    private Button mPauseButton = null;

    private TextView mRecordingPrompt;
    private int mRecordPromptCount = 0;

    private boolean mStartRecording = true;
    private boolean mPauseRecording = true;

    private HyBidInterstitialAd mInterstitial;

    private Chronometer mChronometer = null;
    long timeWhenPaused = 0; //stores time when user clicks pause button

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Record_Fragment.
     */
    public static RecordFragment newInstance(int position) {
        RecordFragment f = new RecordFragment();
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);

        return f;
    }

    public RecordFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View recordView = inflater.inflate(R.layout.fragment_record, container, false);

        mChronometer = recordView.findViewById(R.id.chronometer);
        //update recording prompt text
        mRecordingPrompt = recordView.findViewById(R.id.recording_status_text);

        mRecordButton = recordView.findViewById(R.id.btnRecord);
        mRecordButton.setOnClickListener(v -> {
            onRecord(mStartRecording);
            mStartRecording = !mStartRecording;
        });

        mPauseButton = recordView.findViewById(R.id.btnPause);
        mPauseButton.setVisibility(View.GONE); //hide pause button before recording starts
        mPauseButton.setOnClickListener(v -> {
            onPauseRecord(mPauseRecording);
            mPauseRecording = !mPauseRecording;
        });

        mInterstitial = new HyBidInterstitialAd(getActivity(), "4", this);
        mInterstitial.setVideoSkipOffset(5);

        return recordView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // Recording Start/Stop
    //TODO: recording pause
    private void onRecord(boolean start){
        if (((MainActivity) getActivity()).checkPermissions()) {
            Intent intent = new Intent(getActivity(), RecordingService.class);

            if (start) {

                loadInterstitial();

                // start recording
                mRecordButton.setImageResource(R.drawable.ic_media_stop);
                //mPauseButton.setVisibility(View.VISIBLE);
                Toast.makeText(getActivity(), R.string.toast_recording_start, Toast.LENGTH_SHORT).show();

                File folder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    String path = String.valueOf(getContext().getCacheDir());
                    folder = new File(path);
                } else {
                    folder = new File(Environment.getExternalStorageDirectory() + "/EasySound");
                }

                if (!folder.exists()) {
                    //folder /EasySound doesn't exist, create the folder
                    folder.mkdir();
                }

                //start Chronometer
                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.start();
                mChronometer.setOnChronometerTickListener(chronometer -> {
                    if (mRecordPromptCount == 0) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                    } else if (mRecordPromptCount == 1) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + "..");
                    } else if (mRecordPromptCount == 2) {
                        mRecordingPrompt.setText(getString(R.string.record_in_progress) + "...");
                        mRecordPromptCount = -1;
                    }

                    mRecordPromptCount++;
                });

                //start RecordingService
                getActivity().startService(intent);
                //keep screen on while recording
                getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                mRecordingPrompt.setText(getString(R.string.record_in_progress) + ".");
                mRecordPromptCount++;

            } else {
                //stop recording
                mRecordButton.setImageResource(R.drawable.ic_mic_white_36dp);
                //mPauseButton.setVisibility(View.GONE);
                mChronometer.stop();
                mChronometer.setBase(SystemClock.elapsedRealtime());
                timeWhenPaused = 0;
                mRecordingPrompt.setText(getString(R.string.record_prompt));

                getActivity().stopService(intent);
                //allow the screen to turn off again once recording is finished
                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                showInterstitial();
            }

            disableRecordButtonWithTimeout();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mInterstitial != null) {
            mInterstitial.destroy();
        }
    }

    private void disableRecordButtonWithTimeout() {
        if (getView() != null) {
            mRecordButton.setEnabled(false);
            getView().postDelayed(() -> {
                if (getContext() != null && isResumed()) {
                    mRecordButton.setEnabled(true);
                }
            }, DELAY_RECORD_BUTTON);
        }
    }

    //TODO: implement pause recording
    private void onPauseRecord(boolean pause) {
        if (pause) {
            //pause recording
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds
                    (R.drawable.ic_media_play ,0 ,0 ,0);
            mRecordingPrompt.setText(getString(R.string.resume_recording_button).toUpperCase());
            timeWhenPaused = mChronometer.getBase() - SystemClock.elapsedRealtime();
            mChronometer.stop();
        } else {
            //resume recording
            mPauseButton.setCompoundDrawablesWithIntrinsicBounds
                    (R.drawable.ic_media_pause ,0 ,0 ,0);
            mRecordingPrompt.setText(getString(R.string.pause_recording_button).toUpperCase());
            mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenPaused);
            mChronometer.start();
        }
    }

    public void pauseOnBackPressed(){
        if (mPauseRecording){
            onRecord(false);
        }
    }

    private void loadInterstitial() {
        mInterstitial.load();
    }

    private void showInterstitial() {
        if (mInterstitial != null && mInterstitial.isReady()) {
            mInterstitial.show();
        }
    }

    // HyBid Interstitial Listeners
    @Override
    public void onInterstitialClick() {
        Log.d(LOG_TAG, "onInterstitialClick");
    }

    @Override
    public void onInterstitialDismissed() {
        Log.d(LOG_TAG, "onInterstitialDismissed");
        disableRecordButtonWithTimeout();
    }

    @Override
    public void onInterstitialImpression() {
        Log.d(LOG_TAG, "onInterstitialImpression");
    }

    @Override
    public void onInterstitialLoadFailed(@Nullable Throwable throwable) {
        Log.d(LOG_TAG, "onInterstitialLoadFailed");
    }

    @Override
    public void onInterstitialLoaded() {
        Log.d(LOG_TAG, "onInterstitialLoaded");
    }

    // HyBid Interstitial Video Listeners
    @Override
    public void onVideoError(int i) {
        Log.d(LOG_TAG, "onVideoError");
    }

    @Override
    public void onVideoStarted() {
        Log.d(LOG_TAG, "onVideoStarted");
    }

    @Override
    public void onVideoDismissed(int i) {
        Log.d(LOG_TAG, "onVideoDismissed");
    }

    @Override
    public void onVideoFinished() {
        Log.d(LOG_TAG, "onVideoFinished");
    }

    @Override
    public void onVideoSkipped() {
        Log.d(LOG_TAG, "onVideoSkipped");
    }
}