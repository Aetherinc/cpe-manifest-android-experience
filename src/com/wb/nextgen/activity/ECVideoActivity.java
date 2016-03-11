package com.wb.nextgen.activity;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.RelativeLayout;

import com.wb.nextgen.R;
import com.wb.nextgen.data.DemoData;
import com.wb.nextgen.widget.ECMediaController;

import net.flixster.android.drm.ObservableVideoView;

/**
 * Created by gzcheng on 3/7/16.
 */
public class ECVideoActivity extends AbstractECView {
    protected ObservableVideoView videoView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        videoView = (ObservableVideoView) findViewById(R.id.surface_view);
        videoView.setMediaController(new ECMediaController(this, (RelativeLayout)findViewById(R.id.video_view_container)));
        //videoView.setOnErrorListener(getOnErrorListener());
        videoView.setOnPreparedListener(new PreparedListener());
        //videoView.setOnCompletionListener(getOnCompletionListener());
        videoView.requestFocus();
    }


    private class PreparedListener implements MediaPlayer.OnPreparedListener {
        @Override
        public void onPrepared(MediaPlayer mp) {

            videoView.start();
        }
    }


    @Override
    public int getListItemViewLayoutId(){
        return R.layout.next_gen_ec_list_item;
    }

    public void onResume() {
        super.onResume();
        /*Intent intent = getIntent();
        Uri uri = intent.getData();*/
        videoView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getContentViewId(){
        return R.layout.next_gen_ecs_view;
    }

    @Override
    public void onLeftListItemSelected(DemoData.ECContentData ec){
        if (ec != null) {
            selectedECNameTextView.setText(ec.title);
            videoView.setVideoURI(Uri.parse(ec.ecVideoUrl));
        }
    }
}
