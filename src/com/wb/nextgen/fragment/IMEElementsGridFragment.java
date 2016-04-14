package com.wb.nextgen.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wb.nextgen.NextGenApplication;
import com.wb.nextgen.R;
import com.wb.nextgen.activity.NextGenPlayer;
import com.wb.nextgen.data.DemoData;
import com.wb.nextgen.data.MovieMetaData;
import com.wb.nextgen.interfaces.NextGenFragmentTransactionInterface;
import com.wb.nextgen.interfaces.NextGenPlaybackStatusListener;
import com.wb.nextgen.model.AVGalleryIMEEngine;
import com.wb.nextgen.model.NextGenIMEEngine;
import com.wb.nextgen.model.TheTakeIMEEngine;
import com.wb.nextgen.util.PicassoTrustAll;

import java.util.ArrayList;
import java.util.List;
import com.wb.nextgen.data.MovieMetaData.IMEElementsGroup;

/**
 * Created by gzcheng on 3/28/16.
 */
public class IMEElementsGridFragment extends NextGenGridViewFragment implements NextGenPlaybackStatusListener {

    List<MovieMetaData.IMEElementsGroup> imeGroups;
    final List<NextGenIMEEngine> imeEngines = new ArrayList<NextGenIMEEngine>();
    long currentTimeCode = 0L;



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        imeGroups = NextGenApplication.getMovieMetaData().getImeElementGroups();
        for (IMEElementsGroup group : imeGroups){

            if (group.linkedExperience.getExternalApp() != null){
                if(MovieMetaData.THE_TAKE_MANIFEST_IDENTIFIER.equals(group.linkedExperience.getExternalApp().externalApiName)){
                    imeEngines.add(new TheTakeIMEEngine());
                }else
                    imeEngines.add(new AVGalleryIMEEngine(group.getIMEElementesList()));

            }else
                imeEngines.add(new AVGalleryIMEEngine(group.getIMEElementesList()));
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView()  {
        super.onDestroyView();
    }



    protected void onListItmeClick(View v, int position, long id){
        if (position < 0 || position >= imeGroups.size())
            return;
        IMEElementsGroup group = imeGroups.get(position);
        NextGenIMEEngine engine = imeEngines.get(position);
        Object imeObject = engine.getCurrentIMEElement();


        if (imeObject instanceof MovieMetaData.IMEElement) {
           Object dataObj = ((MovieMetaData.IMEElement)imeObject).imeObject ;
            if (dataObj instanceof MovieMetaData.PresentationDataItem) {
                NextGenPlayer playerActivity = null;
                if (getActivity() instanceof NextGenPlayer) {
                    playerActivity = (NextGenPlayer) getActivity();
                }

                if (playerActivity != null) {
                    if (dataObj instanceof MovieMetaData.ECGalleryItem) {
                        ECGalleryViewFragment fragment = new ECGalleryViewFragment();
                        fragment.setBGImageUrl(DemoData.getExtraBackgroundUrl());
                        fragment.setCurrentGallery((MovieMetaData.ECGalleryItem) dataObj);
                        playerActivity.transitMainFragment(fragment);
                        playerActivity.pausMovieForImeECPiece();


                    } else if (dataObj instanceof MovieMetaData.AudioVisualItem) {
                        ECVideoViewFragment fragment = new ECVideoViewFragment();
                        fragment.setBGImageUrl(DemoData.getExtraBackgroundUrl());
                        fragment.setAudioVisualItem((MovieMetaData.AudioVisualItem) dataObj);
                        playerActivity.transitMainFragment(fragment);
                        playerActivity.pausMovieForImeECPiece();
                    }
                }
            }
        }
    }

    protected int getNumberOfColumns(){
        return 2;
    }

    protected int getListItemCount(){
        return imeGroups.size();
    }

    protected Object getListItemAtPosition(int i){
        return imeGroups.get(i);
    }

    protected int getListItemViewId(){
        return R.layout.ime_grid_item_view;
    }

    protected void fillListRowWithObjectInfo(int position, View rowView, Object item, boolean isSelected){
        /*TextView titleText= (TextView)rowView.findViewById(R.id.ime_title);
        TextView subText1= (TextView)rowView.findViewById(R.id.ime_desc_text1);
        TextView subText2= (TextView)rowView.findViewById(R.id.ime_desc_text2);
        ImageView poster = (ImageView)rowView.findViewById(R.id.ime_image_poster);*/

        IMEElementsGroup group = (IMEElementsGroup)item;
         NextGenIMEEngine engine = imeEngines.get(position);
        if (group.linkedExperience != null){
            rowView.setTag(group.linkedExperience.experienceId);
        }

        localFill(engine, rowView, group);

    }

    private void localFill(NextGenIMEEngine engine, View rowView, IMEElementsGroup group){
        TextView titleText= (TextView)rowView.findViewById(R.id.ime_title);
        TextView subText1= (TextView)rowView.findViewById(R.id.ime_desc_text1);
        TextView subText2= (TextView)rowView.findViewById(R.id.ime_desc_text2);
        ImageView poster = (ImageView)rowView.findViewById(R.id.ime_image_poster);


        if (titleText != null && group.linkedExperience != null){
            titleText.setText(group.linkedExperience.title);      // set a tag with the linked Experience Id
        }

        boolean hasChanged = engine.computeCurrentIMEElement(currentTimeCode);
        Object element = engine.getCurrentIMEElement();
        if (element instanceof MovieMetaData.IMEElement) {
            Object dataObj = ((MovieMetaData.IMEElement) element).imeObject;
            if (dataObj instanceof MovieMetaData.PresentationDataItem) {
                Object currentPresentationId = rowView.getTag(R.id.ime_title);
                rowView.setTag(R.id.ime_title, ((MovieMetaData.PresentationDataItem) dataObj).id);
                if (poster != null) {
                    String imageUrl = ((MovieMetaData.PresentationDataItem) dataObj).getPosterImgUrl();
                    if (poster.getTag() == null || !poster.getTag().equals(imageUrl)) {
                        poster.setTag(imageUrl);
                        PicassoTrustAll.loadImageIntoView(getContext(), imageUrl, poster);
                    }
                }

                if (subText1 != null && !subText1.getText().equals(((MovieMetaData.PresentationDataItem) dataObj).title.toUpperCase())) {
                    subText1.setText(((MovieMetaData.PresentationDataItem) dataObj).title.toUpperCase());
                }
            }
        }

    }

    public void playbackStatusUpdate(final NextGenPlaybackStatus playbackStatus, final long timecode){
        currentTimeCode = timecode;
        if (listAdaptor != null)
            listAdaptor.notifyDataSetChanged();

    }

    protected String getHeaderText(){
        return "";
    }

    protected int getHeaderChildenCount(int header){
        if (header == 0)
            return imeGroups.size();
        else
            return 0;
    }

    protected int getHeaderCount(){
        return 0;
    }

    protected int getStartupSelectedIndex(){
        return -1;
    }
}
