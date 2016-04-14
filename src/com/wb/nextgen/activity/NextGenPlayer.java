package com.wb.nextgen.activity;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.wb.nextgen.R;

import com.wb.nextgen.data.DemoData;
import com.wb.nextgen.fragment.NextGenPlayerBottomFragment;
import com.wb.nextgen.interfaces.NextGenFragmentTransactionInterface;
import com.wb.nextgen.interfaces.NextGenPlaybackStatusListener;
import com.wb.nextgen.network.TheTakeApiDAO;
import com.wb.nextgen.util.PicassoTrustAll;
import com.wb.nextgen.util.TabletUtils;
import com.wb.nextgen.util.concurrent.ResultListener;
import com.wb.nextgen.util.utils.NextGenFragmentTransactionEngine;
import com.wb.nextgen.widget.MainFeatureMediaController;

import net.flixster.android.drm.IVideoViewActionListener;
import net.flixster.android.drm.ObservableVideoView;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;


/**
 * Created by gzcheng on 1/5/16.
 */
public class NextGenPlayer extends AbstractNextGenActivity implements NextGenFragmentTransactionInterface {


    protected ObservableVideoView videoView;

    private TimerTask imeUpdateTask;

    private Timer imeUpdateTimer;

    private Button actionBarLeftButton;

    private MainFeatureMediaController mediaController;

    NextGenFragmentTransactionEngine nextGenFragmentTransactionEngine;

    NextGenPlayerBottomFragment imeBottomFragment;

    public static final Uri INTERSTITIAL_VIDEO_URI = Uri.parse("android.resource://com.wb.nextgen/" + R.raw.mos_nextgen_interstitial);

    private Uri currentUri = null;

    //TextView imeText;
    //IMEElementsGridFragment imeGridFragment;
    private long lastTimeCode = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ActionBar actionBar = getActionBar();
        TypedValue tv = new TypedValue();
        if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
         //   actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
        }
        LayoutInflater inflator = (LayoutInflater) this .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarCustomView = inflator.inflate(R.layout.action_bar_custom_view, null);

        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setStackedBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setLogo(null);
        actionBar.setTitle("");

        //actionBarLeftButton = (Button) actionBarCustomView.findViewById(R.id.action_bar_left_button);
        ImageView centerBanner = (ImageView) actionBarCustomView.findViewById(R.id.action_bar_center_banner);
        ImageView rightLogo = (ImageView) actionBarCustomView.findViewById(R.id.action_bar_right_logo);
        actionBar.setCustomView(actionBarCustomView);

        if (centerBanner != null)
            PicassoTrustAll.loadImageIntoView(this, DemoData.getMovieLogoUrl(), centerBanner);

        actionBarLeftButton = (Button) actionBarCustomView.findViewById(R.id.action_bar_left_button);
        actionBarLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        nextGenFragmentTransactionEngine = new NextGenFragmentTransactionEngine(this);

        setContentView(R.layout.next_gen_videoview);

        backgroundImageView = (ImageView)findViewById(R.id.ime_background_image_view);

        videoView = (ObservableVideoView) findViewById(R.id.surface_view);
        //videoView.setMediaController(mediaController);
        videoView.setOnErrorListener(getOnErrorListener());
        videoView.setOnPreparedListener(getOnPreparedListener());
        videoView.setOnCompletionListener(getOnCompletionListener());
        videoView.requestFocus();
        videoView.setVideoViewListener(new IVideoViewActionListener() {

            @Override
            public void onTimeBarSeekChanged(int currentTime) {
                updateImeFragment(NextGenPlaybackStatusListener.NextGenPlaybackStatus.SEEK, currentTime);
            }

            @Override
            public void onResume() {
                updateImeFragment(NextGenPlaybackStatusListener.NextGenPlaybackStatus.PAUSE, videoView.getCurrentPosition());
            }

            @Override
            public void onPause() {
                updateImeFragment(NextGenPlaybackStatusListener.NextGenPlaybackStatus.RESUME, videoView.getCurrentPosition());
            }
        });

        imeBottomFragment = new NextGenPlayerBottomFragment();


        //transitLeftFragment(new NextGenIMEActorFragment());
        transitMainFragment(imeBottomFragment);

        //imeBottomFragment.setFragmentTransactionInterface(this);


                //imeText = (TextView)findViewById(R.id.next_gen_ime_text);
        /*NextGenIMEActorFragment imeActorFragment = (NextGenIMEActorFragment)getSupportFragmentManager().findFragmentById(R.id.ime_actor_fragment);

        imeGridFragment = (IMEElementsGridFragment)getSupportFragmentManager().findFragmentById(R.id.ime_grid_fragment);
        imeGridFragment.setFragmentTransactionInterface(this);*/

    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();

        if (getSupportFragmentManager().getBackStackEntryCount() == 0 )
            finish();
        else if (getSupportFragmentManager().getBackStackEntryCount() == 1 && isPausedByIME){
            isPausedByIME = false;
            videoView.start();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideShowNextGenView();
    }

    protected void updateImeFragment(final NextGenPlaybackStatusListener.NextGenPlaybackStatus playbackStatus, final long timecode){
        if (lastTimeCode == timecode)
            return;

        lastTimeCode = timecode;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (imeBottomFragment != null)
                    imeBottomFragment.playbackStatusUpdate(playbackStatus, timecode);
                //if (imeText != null)
                 //   imeText.setText(Long.toString(timecode));
            }
        });

        switch (playbackStatus){
            case PREPARED:
                break;
            case STARTED:
                break;
            case STOP:
                break;
            case TIMESTAMP_UPDATE:
                break;
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        hideShowNextGenView();
    }

    public int getLayoutViewId(){
        return R.layout.next_gen_videoview;
    }

    private void hideShowNextGenView(){
        if (TabletUtils.isTablet()) {
            View nextGenView = findViewById(R.id.next_gen_ime_bottom_view);
            if (nextGenView == null)
                return;
            switch (this.getResources().getConfiguration().orientation) {
                case Configuration.ORIENTATION_PORTRAIT:

                    nextGenView.setVisibility(View.VISIBLE);
                    if (mediaController != null)
                        mediaController.hideShowControls(true);
                    break;
                 case Configuration.ORIENTATION_LANDSCAPE:
                    nextGenView.setVisibility(View.GONE);
                    if (mediaController != null)
                        mediaController.hideShowControls(false);
            }
        }
    }

    private class ErrorListener implements MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {


            return true;
        }
    }


    protected MediaPlayer.OnErrorListener getOnErrorListener(){
        return new ErrorListener();
    }

    private class PreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {

            videoView.start();
            if (imeUpdateTimer == null){
                imeUpdateTimer = new Timer();
            }
            if (imeUpdateTask == null){
                imeUpdateTask = new TimerTask() {
                    @Override
                    public void run() {
                        updateImeFragment(NextGenPlaybackStatusListener.NextGenPlaybackStatus.TIMESTAMP_UPDATE, videoView.getCurrentPosition());
                    }
                };
                imeUpdateTimer.scheduleAtFixedRate(imeUpdateTask, 0, 1000);
            }


            updateImeFragment(NextGenPlaybackStatusListener.NextGenPlaybackStatus.PREPARED, -1L);

        }
    }


    protected MediaPlayer.OnPreparedListener getOnPreparedListener(){
        return new PreparedListener();
    }

    private class CompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
            updateImeFragment(NextGenPlaybackStatusListener.NextGenPlaybackStatus.STOP, -1L);
            if (currentUri.equals(INTERSTITIAL_VIDEO_URI)){
                playMainMovie();
            }else
                finish();

        }
    }

    private void playMainMovie(){
        if (INTERSTITIAL_VIDEO_URI.equals(currentUri)) {
            videoView.setOnTouchListener(null);
            Intent intent = getIntent();
            Uri uri = intent.getData();
            currentUri = uri;
            videoView.setVideoURI(uri);
            if (mediaController == null) {
                mediaController = new MainFeatureMediaController(this);
                videoView.setMediaController(mediaController);
            }
        }
    }

    protected MediaPlayer.OnCompletionListener getOnCompletionListener(){
        return new CompletionListener();
    }

    public void onResume() {
        super.onResume();
        videoView.setVisibility(View.VISIBLE);
        if (currentUri == null) {
            currentUri = INTERSTITIAL_VIDEO_URI;
            videoView.setVideoURI(currentUri);
            videoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    playMainMovie();
                    return true;
                }
            });
        } else{
            videoView.seekTo(resumePlayTime);
            /*Intent intent = getIntent();
            Uri uri = intent.getData();
            videoView.setVideoURI(uri);*/
        }
        hideShowNextGenView();
    }

    int resumePlayTime = 0;
    @Override
    public void onPause(){
        resumePlayTime = videoView.getCurrentPosition();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //ContentLocker content = FlixsterApplication.getCurrentPlayableContent();
        super.onDestroy();
        if (imeUpdateTask != null) {
            imeUpdateTask.cancel();
            imeUpdateTask = null;
        }
        if (imeUpdateTimer != null){
            imeUpdateTimer.cancel();
            imeUpdateTimer = null;
        }
        //FlixsterApplication.setCurrentPlayableContent(content);

    }

    @Override
    public void resetUI(boolean isRoot){

    }

    public int getMainFrameId(){
        return R.id.next_gen_ime_bottom_view;//next_gen_ime_main_frame;
    }

    public int getLeftFrameId(){
        return R.id.next_gen_ime_bottom_view;
    }

    public int getRightFrameId(){
        return R.id.next_gen_ime_bottom_view;

    }

    boolean isPausedByIME = false;
    public void pausMovieForImeECPiece(){
        videoView.pause();
        isPausedByIME = true;
    }

    @Override
    public void transitLeftFragment(Fragment nextFragment){
        nextGenFragmentTransactionEngine.transitFragment(getSupportFragmentManager(), getLeftFrameId(), nextFragment);
    }

    @Override
    public void transitRightFragment(Fragment nextFragment){
        nextGenFragmentTransactionEngine.transitFragment(getSupportFragmentManager(), getRightFrameId(), nextFragment);
    }

    @Override
    public void transitMainFragment(Fragment nextFragment){
        nextGenFragmentTransactionEngine.transitFragment(getSupportFragmentManager(), getMainFrameId(), nextFragment);
    }

    @Override
    public int getLeftButtonLogoId(){
        return R.drawable.home_logo;
    }

    @Override
    public String getBackgroundImgUri(){
        return DemoData.getExtraBackgroundUrl();
    }

    @Override
    public String getLeftButtonText(){
        return getResources().getString(R.string.home_button_text);
    }

    public String getRightTitleImageUri(){
        return "";

    }
}
