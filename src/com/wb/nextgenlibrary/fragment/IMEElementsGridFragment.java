package com.wb.nextgenlibrary.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wb.nextgenlibrary.NextGenExperience;
import com.wb.nextgenlibrary.R;
import com.wb.nextgenlibrary.activity.NextGenPlayer;
import com.wb.nextgenlibrary.data.MovieMetaData;
import com.wb.nextgenlibrary.data.MovieMetaData.IMEElementsGroup;
import com.wb.nextgenlibrary.data.TheTakeData;
import com.wb.nextgenlibrary.data.TheTakeData.TheTakeProductFrame;
import com.wb.nextgenlibrary.interfaces.IMEVideoStatusListener;
import com.wb.nextgenlibrary.interfaces.NextGenPlaybackStatusListener;
import com.wb.nextgenlibrary.model.AVGalleryIMEEngine;
import com.wb.nextgenlibrary.model.NextGenIMEEngine;
import com.wb.nextgenlibrary.model.TheTakeIMEEngine;
import com.wb.nextgenlibrary.network.TheTakeApiDAO;
import com.wb.nextgenlibrary.util.TabletUtils;
import com.wb.nextgenlibrary.util.concurrent.ResultListener;
import com.wb.nextgenlibrary.util.utils.NextGenGlide;
import com.wb.nextgenlibrary.util.utils.StringHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gzcheng on 3/28/16.
 */
public class IMEElementsGridFragment extends NextGenGridViewFragment implements NextGenPlaybackStatusListener, IMEVideoStatusListener {

	List<IMEElementsGroup> imeGroups;
    final List<NextGenIMEEngine> imeEngines = new ArrayList<NextGenIMEEngine>();
    long currentTimeCode = 0L;

    List<IMEDisplayObject> activeIMEs = new ArrayList<IMEDisplayObject>();

    private class IMEDisplayObject{
        final MovieMetaData.ExperienceData imeExperience;
        final Object imeObject;
        final String title;

        public IMEDisplayObject(MovieMetaData.ExperienceData experienceData, Object imeObject){
            this.title = experienceData.title;
            this.imeObject = imeObject;
            this.imeExperience = experienceData;
        }
    }

    Bundle savedInstanceState = null;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        imeGroups = NextGenExperience.getMovieMetaData().getImeElementGroups();
        this.savedInstanceState = savedInstanceState;
        for (IMEElementsGroup group : imeGroups){

            if (group.getExternalApiData() != null){
                if(MovieMetaData.THE_TAKE_MANIFEST_IDENTIFIER.equals(group.getExternalApiData().externalApiName)){
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

    public void onVideoStartPlaying(){
        NextGenPlayer playerActivity = null;
        if (getActivity() instanceof NextGenPlayer) {
            playerActivity = (NextGenPlayer) getActivity();
            playerActivity.pauseMovieForImeECPiece();
        }
    }
    public void onFragmentDestroyed(){

    }

    protected void onListItemClick(View v, int position, long id){
        if (position < 0 || position >= activeIMEs.size())
            return;
        IMEDisplayObject activeObj = activeIMEs.get(position);
        NextGenPlayer playerActivity = null;
        if (getActivity() instanceof NextGenPlayer) {
            playerActivity = (NextGenPlayer) getActivity();
        }

        if (activeObj.imeObject instanceof MovieMetaData.IMEElement) {
            Object dataObj = ((MovieMetaData.IMEElement)activeObj.imeObject).imeObject ;
            if (dataObj instanceof MovieMetaData.PresentationDataItem) {
                MovieMetaData.PresentationDataItem headElement = (MovieMetaData.PresentationDataItem) dataObj;

                if (dataObj instanceof AVGalleryIMEEngine.IMECombineItem){
                    headElement = ((AVGalleryIMEEngine.IMECombineItem)dataObj).getAllPresentationItems().get(0);
                }

                if (playerActivity != null) {
                    if (headElement instanceof MovieMetaData.ECGalleryItem) {
                        ECGalleryViewFragment fragment = new ECGalleryViewFragment();
                        fragment.setShouldShowCloseBtn(true);
                        if (NextGenExperience.getMovieMetaData().getInMovieExperience().style != null)
                            fragment.setBGImageUrl(NextGenExperience.getMovieMetaData().getInMovieExperience().style.getBackground().getImage().url);
                        fragment.setCurrentGallery((MovieMetaData.ECGalleryItem) headElement);
                        playerActivity.transitMainFragment(fragment);
                        //playerActivity.pausMovieForImeECPiece();


                    } else if (headElement instanceof MovieMetaData.AudioVisualItem) {

                        if (((MovieMetaData.AudioVisualItem) headElement).isShareClip()){
                            ShareClipFragment fragment = new ShareClipFragment();
                            fragment.setShouldAutoPlay(false);
                            fragment.setShouldShowCloseBtn(true);
                            fragment.setExperienceAndIndex(activeObj.imeExperience, ((MovieMetaData.IMEElement) activeObj.imeObject).itemIndex);
                            if (NextGenExperience.getMovieMetaData().getInMovieExperience().style != null)
                                fragment.setBGImageUrl(NextGenExperience.getMovieMetaData().getInMovieExperience().style.getBackground().getImage().url);
                            fragment.setVideoStatusListener(this);
                            playerActivity.transitMainFragment(fragment);
                        }else {

                            ECVideoViewFragment fragment = new ECVideoViewFragment();
                            fragment.setShouldShowCloseBtn(true);
                            fragment.setShouldAutoPlay(false);
                            if (NextGenExperience.getMovieMetaData().getInMovieExperience().style != null)
                                fragment.setBGImageUrl(NextGenExperience.getMovieMetaData().getInMovieExperience().style.getBackground().getImage().url);
                            fragment.setAudioVisualItem((MovieMetaData.AudioVisualItem) headElement);
                            fragment.setVideoStatusListener(this);
                            playerActivity.transitMainFragment(fragment);
                        }
                    } else if (headElement instanceof MovieMetaData.LocationItem ||
                            (headElement instanceof AVGalleryIMEEngine.IMECombineItem && ((AVGalleryIMEEngine.IMECombineItem)headElement).isLocation() ) ){

                        // TODO: deal with multiple locations at the same timecode later on.
                        if (headElement instanceof AVGalleryIMEEngine.IMECombineItem){
                            headElement = ((AVGalleryIMEEngine.IMECombineItem)headElement).getAllPresentationItems().get(0);
                        }
                        IMEECMapViewFragment fragment = new IMEECMapViewFragment();
                        fragment.setShouldShowCloseBtn(true);
                        fragment.setLocationItem(activeObj.title, (MovieMetaData.LocationItem)headElement);
                        playerActivity.transitMainFragment(fragment);
                    } else if (dataObj instanceof MovieMetaData.TriviaItem){
                        ECTrviaViewFragment fragment = new ECTrviaViewFragment();
                        fragment.setShouldShowCloseBtn(true);
                        fragment.setTriviaItem(activeObj.title, (MovieMetaData.TriviaItem)dataObj);
                        playerActivity.transitMainFragment(fragment);

                    } else if (dataObj instanceof MovieMetaData.TextItem) {
						ECTextViewFragment fragment = new ECTextViewFragment();
						fragment.setShouldShowCloseBtn(true);
						fragment.setTextItem(activeObj.title, (MovieMetaData.TextItem)dataObj);
						playerActivity.transitMainFragment(fragment);
					}
                }
            }
        } else if (activeObj.imeObject instanceof TheTakeProductFrame){
            if (playerActivity != null){
                TheTakeFrameProductsFragment fragment = new TheTakeFrameProductsFragment();
                fragment.setShouldShowCloseBtn(true);
                fragment.setTitleText(activeObj.title.toUpperCase());
                fragment.setFrameProductTime(((TheTakeProductFrame)activeObj.imeObject).frameTime);
                playerActivity.transitMainFragment(fragment);
            }
        }
    }

    protected int getNumberOfColumns(){
        return TabletUtils.isTablet() ? 2 : 1;
    }

    protected int getListItemCount(){
        return activeIMEs.size();
    }

    protected Object getListItemAtPosition(int i){
        return activeIMEs.get(i);
    }
    protected int getListItemViewId(){      // not using this
        return 0;
    }

    protected int getListItemViewId(int position){
        IMEDisplayObject activeObj = activeIMEs.get(position);

        int retId = R.layout.ime_grid_presentation_item;

        if (activeObj.imeObject instanceof MovieMetaData.IMEElement) {
            Object dataObj = ((MovieMetaData.IMEElement) activeObj.imeObject).imeObject;
            if (dataObj instanceof MovieMetaData.PresentationDataItem) {
                if (dataObj instanceof MovieMetaData.LocationItem ||
                        (dataObj instanceof AVGalleryIMEEngine.IMECombineItem && ((AVGalleryIMEEngine.IMECombineItem)dataObj).isLocation() ) )
                    retId = R.layout.ime_grid_presentation_item;
                else if (dataObj instanceof MovieMetaData.AudioVisualItem &&
                        ((MovieMetaData.AudioVisualItem)dataObj).isShareClip()) {
                    retId = R.layout.ime_grid_share_item;
                } else if (dataObj instanceof MovieMetaData.TextItem) {
					retId = R.layout.ime_grid_text_item;
				}
            }
        }else if (activeObj.imeObject instanceof TheTakeProductFrame){
            retId = R.layout.ime_grid_shop_item;
        }

        return retId;
    }


    protected void fillListRowWithObjectInfo(int position, View rowView, Object item, boolean isSelected){
        IMEDisplayObject activeObj = (IMEDisplayObject)item;

        TextView titleText = (TextView)rowView.findViewById(R.id.ime_title);
        final TextView subText1 = (TextView)rowView.findViewById(R.id.ime_desc_text1);
        final ImageView poster = (ImageView)rowView.findViewById(R.id.ime_image_poster);

        titleText.setText(activeObj.title.toUpperCase());      // set a tag with the linked Experience Id

        if (activeObj.imeObject instanceof MovieMetaData.IMEElement) {
            Object dataObj = ((MovieMetaData.IMEElement) activeObj.imeObject).imeObject;
            if (dataObj instanceof MovieMetaData.PresentationDataItem) {

                rowView.setTag(R.id.ime_title, ((MovieMetaData.PresentationDataItem) dataObj).getId());
                if (dataObj instanceof MovieMetaData.LocationItem ){

                    MovieMetaData.LocationItem locationItem = (MovieMetaData.LocationItem) dataObj;
                    if (locationItem != null && poster.getHeight() > 0 && poster.getWidth() > 0){
                        String imageUrl = locationItem.getGoogleMapImageUrl(poster.getWidth(), poster.getHeight());
                        NextGenGlide.load(getActivity(), imageUrl).centerCrop().into(poster);
                    }

                } else if (poster != null) {
                    String imageUrl = ((MovieMetaData.PresentationDataItem) dataObj).getPosterImgUrl();
					if (!StringHelper.isEmpty(imageUrl)) {
                        NextGenGlide.load(getActivity(), imageUrl).centerCrop()
								.into(poster);
						poster.setVisibility(View.VISIBLE);
					} else {
						poster.setVisibility(View.GONE);
					}
                }

                ImageView playbutton = (ImageView)rowView.findViewById(R.id.ime_item_play_logo);
                if (playbutton != null){
                    playbutton.setVisibility((dataObj instanceof MovieMetaData.AudioVisualItem) ? View.VISIBLE : View.INVISIBLE);
                }

                if (subText1 != null && !subText1.getText().equals(((MovieMetaData.PresentationDataItem) dataObj).getTitle())) {
                    subText1.setText(((MovieMetaData.PresentationDataItem) dataObj).getTitle());
                    subText1.setTag(R.id.ime_title, "");
                }
            }
        }else if (activeObj.imeObject instanceof TheTakeProductFrame){
            final int frameTime = ((TheTakeProductFrame) activeObj.imeObject).frameTime;
            if  (rowView.getTag(R.id.ime_title) == null || !rowView.getTag(R.id.ime_title).equals(frameTime))
            {
                rowView.setTag(R.id.ime_title, ((TheTakeProductFrame) activeObj.imeObject).frameTime);
                if (poster != null) {

                    poster.setImageDrawable(null);

                }

                if (subText1 != null && (subText1.getTag(R.id.ime_title) == null || !subText1.getTag(R.id.ime_title).equals(frameTime))) {
                    subText1.setText("");
                    subText1.setTag(R.id.ime_title, frameTime);
                }

                TheTakeApiDAO.getFrameProducts(frameTime, new ResultListener<List<TheTakeData.TheTakeProduct>>() {
                    @Override
                    public void onResult(final List<TheTakeData.TheTakeProduct> result) {
                        if (subText1.getTag(R.id.ime_title).equals(frameTime)) {
                            if (result != null && result.size() > 0 && getActivity() != null) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        subText1.setText(result.get(0).productName);
                                        poster.setBackgroundColor(getResources().getColor(android.R.color.white));
                                        NextGenGlide.load(getActivity(), result.get(0).getProductThumbnailUrl()).into(poster);

                                    }
                                });
                            }
                        }

                    }

                    @Override
                    public <E extends Exception> void onException(E e) {
                        subText1.setText("");

                    }
                });
            }


        }

    }

    public void playbackStatusUpdate(final NextGenPlaybackStatus playbackStatus, final long timecode){
        currentTimeCode = timecode;

        List<IMEDisplayObject> objList = new ArrayList<IMEDisplayObject>();
        for(int i = 0 ; i< imeEngines.size(); i++){
            NextGenIMEEngine engine = imeEngines.get(i);
            boolean hasChanged =  engine.computeCurrentIMEElement(currentTimeCode);
            List<Object> elements = engine.getCurrentIMEItems();


            if (elements != null && elements.size() > 0){
                for (Object element : elements)
                    objList.add(new IMEDisplayObject(imeGroups.get(i).linkedExperience, element));
            }
        }
        activeIMEs = objList;

        if (listAdaptor != null)
            listAdaptor.notifyDataSetChanged();

    }

    protected String getHeaderText(){
        return "";
    }

    protected int getHeaderChildenCount(int header){
        if (header == 0)
            return activeIMEs.size();
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
