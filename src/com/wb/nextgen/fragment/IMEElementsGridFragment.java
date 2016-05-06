package com.wb.nextgen.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wb.nextgen.NextGenApplication;
import com.wb.nextgen.R;
import com.wb.nextgen.activity.NextGenPlayer;
import com.wb.nextgen.data.DemoData;
import com.wb.nextgen.data.MovieMetaData;
import com.wb.nextgen.data.TheTakeData.TheTakeProductFrame;
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


    List<IMEDisplayObject> activeIMEs = new ArrayList<IMEDisplayObject>();

    private class IMEDisplayObject{
        final Object imeObject;
        final String title;

        public IMEDisplayObject(String title, Object imeObject){
            this.title = title;
            this.imeObject = imeObject;
        }
    }

    Bundle savedInstanceState = null;
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        imeGroups = NextGenApplication.getMovieMetaData().getImeElementGroups();
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



    protected void onListItmeClick(View v, int position, long id){
        if (position < 0 || position >= activeIMEs.size())
            return;
        IMEDisplayObject activeObj = activeIMEs.get(position);
        //NextGenIMEEngine engine = imeEngines.get(position);
        //Object imeObject = engine.getCurrentIMEElement();
        NextGenPlayer playerActivity = null;
        if (getActivity() instanceof NextGenPlayer) {
            playerActivity = (NextGenPlayer) getActivity();
        }

        if (activeObj.imeObject instanceof MovieMetaData.IMEElement) {
           Object dataObj = ((MovieMetaData.IMEElement)activeObj.imeObject).imeObject ;
            if (dataObj instanceof MovieMetaData.PresentationDataItem) {


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
                    } else  if (dataObj instanceof  MovieMetaData.TextItem){
                        ECTrviaViewFragment fragment = new ECTrviaViewFragment();
                        fragment.setTextItem(activeObj.title, (MovieMetaData.TextItem)dataObj);
                        playerActivity.transitMainFragment(fragment);
                        playerActivity.pausMovieForImeECPiece();
                    } else if (dataObj instanceof  MovieMetaData.LocationItem){
                        ECMapViewFragment fragment = new ECMapViewFragment();
                        fragment.setLocationItem(activeObj.title, (MovieMetaData.LocationItem)dataObj);
                        playerActivity.transitMainFragment(fragment);
                        playerActivity.pausMovieForImeECPiece();
                    }
                }
            }
        } else if (activeObj.imeObject instanceof TheTakeProductFrame){
            if (playerActivity != null){
                TheTakeFrameProductsFragment fragment = new TheTakeFrameProductsFragment();
                fragment.setFrameProductTime(((TheTakeProductFrame)activeObj.imeObject).frameTime);
                playerActivity.transitMainFragment(fragment);
                playerActivity.pausMovieForImeECPiece();
            }
        }
    }

    protected int getNumberOfColumns(){
        return 2;
    }

    protected int getListItemCount(){
        return activeIMEs.size();
    }

    protected Object getListItemAtPosition(int i){
        return activeIMEs.get(i);
    }

    protected int getListItemViewId(){
        return R.layout.ime_grid_item_view;
    }

    protected void fillListRowWithObjectInfo(int position, View rowView, Object item, boolean isSelected){

        localFill((IMEDisplayObject)item, rowView);
    }

    protected void setupNewContentView(View view){
        MapView mapView = (MapView)view.findViewById(R.id.ime_map_view);
        mapView.onCreate(savedInstanceState);

    }

    private void localFill(final IMEDisplayObject activeObj, View rowView){
        TextView titleText= (TextView)rowView.findViewById(R.id.ime_title);
        TextView subText1= (TextView)rowView.findViewById(R.id.ime_desc_text1);
        TextView subText2= (TextView)rowView.findViewById(R.id.ime_desc_text2);
        ImageView poster = (ImageView)rowView.findViewById(R.id.ime_image_poster);
        MapView mapView = (MapView)rowView.findViewById(R.id.ime_map_view);


        //if (titleText != null && group.linkedExperience != null){
            titleText.setText(activeObj.title.toUpperCase());      // set a tag with the linked Experience Id
        //}

       // boolean hasChanged =  engine.computeCurrentIMEElement(currentTimeCode);
        //Object element = engine.getCurrentIMEElement();
        if (activeObj.imeObject instanceof MovieMetaData.IMEElement) {
            Object dataObj = ((MovieMetaData.IMEElement) activeObj.imeObject).imeObject;
            if (dataObj instanceof MovieMetaData.PresentationDataItem) {
                Object currentPresentationId = rowView.getTag(R.id.ime_title);
                rowView.setTag(R.id.ime_title, ((MovieMetaData.PresentationDataItem) dataObj).id);
                if (dataObj instanceof MovieMetaData.LocationItem){
                    mapView.setVisibility(View.VISIBLE);
                    poster.setVisibility(View.GONE);
                    final MovieMetaData.LocationItem locationItem = (MovieMetaData.LocationItem)dataObj;
                    //mapView.getMaps
                    final LatLng location = new LatLng(locationItem.latitude, locationItem.longitude);

                    mapView.getMapAsync(new OnMapReadyCallback() {
                        @Override
                        public void onMapReady(GoogleMap googleMap) {

                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 14.0f));   // set location

                            BitmapDescriptor bmDes = BitmapDescriptorFactory.fromBitmap(NextGenApplication.getMovieMetaData().getMapPinBitmap());
                            googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                            googleMap.addMarker(new MarkerOptions()
                                    .position(location)
                                    .icon(bmDes));
                            googleMap.getUiSettings().setMapToolbarEnabled(false);
                            final String ecGroupTitle = activeObj.title;
                            googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                                @Override
                                public void onMapClick(LatLng latLng) {
                                    NextGenPlayer playerActivity = null;
                                    if (getActivity() instanceof NextGenPlayer) {
                                        playerActivity = (NextGenPlayer) getActivity();
                                    }
                                    ECMapViewFragment fragment = new ECMapViewFragment();
                                    fragment.setLocationItem(ecGroupTitle, locationItem);
                                    playerActivity.transitMainFragment(fragment);
                                    playerActivity.pausMovieForImeECPiece();
                                }
                            });
                            //googleMap.addMarker(new MarkerOptions().position(new LatLng(locationItem.latitude, locationItem.longitude)).title("Marker"));
                        }
                    });


                    /*Criteria criteria = new Criteria();
                    LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
                    String bestProvider = locationManager.getBestProvider(criteria, true);
                    Location location = locationManager.getLastKnownLocation(bestProvider);*/



                }else if (poster != null) {
                    poster.setVisibility(View.VISIBLE);
                    mapView.setVisibility(View.GONE);
                    String imageUrl = ((MovieMetaData.PresentationDataItem) dataObj).getPosterImgUrl();
                    if (poster.getTag() == null || !poster.getTag().equals(imageUrl)) {
                        poster.setTag(imageUrl);
                        PicassoTrustAll.loadImageIntoView(getContext(), imageUrl, poster);
                    }
                }

                if (subText1 != null && !subText1.getText().equals(((MovieMetaData.PresentationDataItem) dataObj).title)) {
                    subText1.setText(((MovieMetaData.PresentationDataItem) dataObj).title);
                }
            }
        }else if (activeObj.imeObject instanceof TheTakeProductFrame){
            poster.setVisibility(View.VISIBLE);
            mapView.setVisibility(View.GONE);
            rowView.setTag(R.id.ime_title, ((TheTakeProductFrame) activeObj.imeObject).frameTime);
            if (poster != null) {
                String imageUrl = ((TheTakeProductFrame) activeObj.imeObject).frameImages.image1000px;
                if (poster.getTag() == null || !poster.getTag().equals(imageUrl)) {
                    poster.setTag(imageUrl);
                    PicassoTrustAll.loadImageIntoView(getContext(), imageUrl, poster);
                }
                if (subText1 != null) {
                    subText1.setText("");
                }
            }
        }

    }

    public void playbackStatusUpdate(final NextGenPlaybackStatus playbackStatus, final long timecode){
        currentTimeCode = timecode;

        List<IMEDisplayObject> objList = new ArrayList<IMEDisplayObject>();
        for(int i = 0 ; i< imeEngines.size(); i++){
            NextGenIMEEngine engine = imeEngines.get(i);
            boolean hasChanged =  engine.computeCurrentIMEElement(currentTimeCode);
            Object element = engine.getCurrentIMEElement();
            if (element != null){
                objList.add(new IMEDisplayObject(imeGroups.get(i).linkedExperience.title, element));
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
