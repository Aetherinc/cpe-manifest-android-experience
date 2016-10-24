package com.wb.nextgenlibrary.activity;

import android.os.Bundle;
import android.widget.TextView;

import com.wb.nextgenlibrary.R;
import com.wb.nextgenlibrary.analytic.NextGenAnalyticData;
import com.wb.nextgenlibrary.data.MovieMetaData;
import com.wb.nextgenlibrary.fragment.ECVideoViewFragment;


/**
 * Created by gzcheng on 3/7/16.
 */
public class ECVideoActivity extends AbstractECView implements ECVideoViewFragment.ECVideoListAdaptor{

	ECVideoViewFragment rightVideoFrame;
    protected TextView selectedECNameTextView;
    protected TextView descriptionTextView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        rightVideoFrame = (ECVideoViewFragment) getSupportFragmentManager().findFragmentById(R.id.ec_video_view_fragment);
        rightVideoFrame.setShouldAutoPlay(false);
        rightVideoFrame.setEcsAdaptor(this);

		selectedECNameTextView = (TextView) findViewById(R.id.ec_content_name);
		descriptionTextView = (TextView) findViewById(R.id.ec_content_description);
    }

    @Override
    public void onDestroy() {
        if (rightVideoFrame != null)
            rightVideoFrame.setEcsAdaptor(null);
        super.onDestroy();
    }

    @Override
    public int getListItemViewLayoutId(){
        return R.layout.next_gen_ec_list_item;
    }


    @Override
    public int getContentViewId(){
        return R.layout.next_gen_ecs_view;
    }

    @Override
    public void onLeftListItemSelected(MovieMetaData.ExperienceData ec){
        if (selectedEC == ec)      // avoid video reload
            return;
        else
            selectedEC = ec;
        if (ec != null && ec.audioVisualItems.size() > 0) {
			MovieMetaData.AudioVisualItem audioVisualItem = ec.audioVisualItems.get(0);
			rightVideoFrame.setAudioVisualItem(audioVisualItem);
			if (selectedECNameTextView != null)
				selectedECNameTextView.setText(audioVisualItem.getTitle());
			if (descriptionTextView != null)
				descriptionTextView.setText(audioVisualItem.getSummary());
            NextGenAnalyticData.reportEvent(this, null, "EC Video", NextGenAnalyticData.AnalyticAction.ACTION_CLICK, ec.title);
        }
    }

    @Override
    public void onRequestToggleFullscreen() {
        super.onRequestToggleFullscreen();
    }

    @Override
    public void onFullScreenChange(boolean bFullscreen){
        if (!bFullscreen && getSupportActionBar() != null) {

            getSupportActionBar().hide();
        }
        rightVideoFrame.onFullScreenChange(bFullscreen);
    }

    @Override
    public void playbackFinished(){
        listFragment.selectNextItem();
    }

    @Override
    public boolean shouldStartCountDownForNext(){
        return listFragment.hasReachedLastItem();
    }
}
