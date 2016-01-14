package com.wb.nextgen;


import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.flixster.android.captioning.CaptionedPlayer;
import com.wb.nextgen.R;

import com.wb.nextgen.util.PicassoTrustAll;

/**
 * Created by gzcheng on 1/7/16.
 */
public class NextGenStartupUpperFragment extends Fragment implements View.OnClickListener {
    Button playMovieButton;
    Button extraButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.next_gen_start_upper, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView bg = (ImageView)view.findViewById(R.id.next_gen_startup_layout);
        if (bg != null){
            PicassoTrustAll.loadImageIntoView(NextGenApplication.getContext(), "http://www.manofsteel.com/img/about/full_bg.jpg", bg);
        }
        playMovieButton = (Button) view.findViewById(R.id.next_gen_startup_play_button);
        if (playMovieButton != null){
            playMovieButton.setOnClickListener(this);
        }
        extraButton = (Button) view.findViewById(R.id.next_gen_startup_extra_button);
        if (extraButton != null){
            extraButton.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.next_gen_startup_play_button:
                Intent intent = new Intent(getActivity(), NextGenPlayer.class);
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("https://ia802304.us.archive.org/17/items/BigBuckBunny1280x720Stereo/big_buck_bunny_720_stereo.mp4"), "video/*");
                startActivity(intent);
                //Drm.manager().playMovie(getActivity(), FlixsterApplication.getCurrentPlayableContent(), PhysicalAsset.Definition.HD, "en_US", "en_US");
                //        lockOrientation();
                break;
            case R.id.next_gen_startup_extra_button:
                Intent extraIntent = new Intent(getActivity(), NextGenExtraActivity.class);
                startActivity(extraIntent);
                break;
        }
    }
}
