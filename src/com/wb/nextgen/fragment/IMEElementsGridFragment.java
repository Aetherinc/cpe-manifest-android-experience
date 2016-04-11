package com.wb.nextgen.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.wb.nextgen.NextGenApplication;
import com.wb.nextgen.R;
import com.wb.nextgen.data.DemoData;
import com.wb.nextgen.data.MovieMetaData;
import com.wb.nextgen.interfaces.NextGenFragmentTransactionInterface;
import com.wb.nextgen.interfaces.NextGenPlaybackStatusListener;
import com.wb.nextgen.model.NextGenIMEEngine;
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

    NextGenFragmentTransactionInterface fragmentTransaction = null;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        imeGroups = NextGenApplication.getMovieMetaData().getImeElementGroups();
        for (IMEElementsGroup group : imeGroups){
            imeEngines.add(new NextGenIMEEngine(group.getIMEElementesList()));
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onDestroyView()  {
        super.onDestroyView();
    }

    public void setFragmentTransactionInterface(NextGenFragmentTransactionInterface fragmentTransaction){
        this.fragmentTransaction = fragmentTransaction;

    }

    protected void onListItmeClick(View v, int position, long id){
        if (position < 0 || position >= imeGroups.size())
            return;
        IMEElementsGroup group = imeGroups.get(position);
        NextGenIMEEngine engine = imeEngines.get(position);
        Object imeObject = engine.getCurrentIMEElement();



        if (imeObject instanceof MovieMetaData.PresentationDataItem){
            if (fragmentTransaction == null && getActivity() instanceof NextGenFragmentTransactionInterface){
                fragmentTransaction = (NextGenFragmentTransactionInterface)getActivity();
            }

            if (fragmentTransaction != null) {
                if (imeObject instanceof MovieMetaData.ECGalleryItem) {
                    ECGalleryViewFragment fragment = new ECGalleryViewFragment();
                    fragment.setBGImageUrl( DemoData.getExtraBackgroundUrl());
                    fragment.setCurrentGallery((MovieMetaData.ECGalleryItem) imeObject);
                    fragmentTransaction.transitMainFragment(fragment);


                } else if (imeObject instanceof MovieMetaData.AudioVisualItem) {
                    ECVideoViewFragment fragment = new ECVideoViewFragment();
                    fragment.setBGImageUrl( DemoData.getExtraBackgroundUrl());
                    fragment.setAudioVisualItem((MovieMetaData.AudioVisualItem)imeObject);
                    fragmentTransaction.transitMainFragment(fragment);
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
        Object imeObj = engine.getCurrentIMEElement();

        if (imeObj instanceof MovieMetaData.PresentationDataItem) {
            Object currentPresentationId = rowView.getTag(R.id.ime_title);
            rowView.setTag(R.id.ime_title, ((MovieMetaData.PresentationDataItem) imeObj).id);
            if (poster != null) {
                String imageUrl = ((MovieMetaData.PresentationDataItem) imeObj).getPosterImgUrl();
                if (poster.getTag() == null ||  !poster.getTag().equals(imageUrl)) {
                    poster.setTag(imageUrl);
                    PicassoTrustAll.loadImageIntoView(getContext(), imageUrl, poster);
                }
            }

            if (subText1 != null && !subText1.getText().equals(((MovieMetaData.PresentationDataItem) imeObj).title)) {
                subText1.setText(((MovieMetaData.PresentationDataItem) imeObj).title);
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
