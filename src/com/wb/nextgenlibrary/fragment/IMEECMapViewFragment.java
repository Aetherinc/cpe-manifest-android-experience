package com.wb.nextgenlibrary.fragment;

import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.wb.nextgenlibrary.R;
import com.wb.nextgenlibrary.activity.NextGenPlayer;
import com.wb.nextgenlibrary.data.MovieMetaData;
import com.wb.nextgenlibrary.util.HttpImageHelper;
import com.wb.nextgenlibrary.util.concurrent.ResultListener;
import com.wb.nextgenlibrary.util.utils.F;
import com.wb.nextgenlibrary.util.utils.NextGenLogger;
import com.wb.nextgenlibrary.util.utils.StringHelper;
import com.wb.nextgenlibrary.widget.FixedAspectRatioFrameLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by gzcheng on 3/31/16.
 */
public class IMEECMapViewFragment extends AbstractNextGenFragment {
    protected MapView mapView;

    protected TextView ecTitle;
    protected RecyclerView sceneLocationECRecyclerView;
    protected ECVideoViewFragment videoViewFragment;
    protected ECGalleryViewFragment galleryViewFragment;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    private FrameLayout videoFrame, galleryFrame;

    MovieMetaData.LocationItem selectedLocationItem = null;
    String title = null;

    Bundle savedInstanceState;
    public int getContentViewId(){
        return R.layout.ec_map_view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        mapView = (MapView) view.findViewById(R.id.ec_mapview);
        if(mapView != null)
            mapView.onCreate(savedInstanceState);
        ecTitle = (TextView) view.findViewById(R.id.ec_title_name);
        if (selectedLocationItem != null && title != null){
            setLocationItem(title, selectedLocationItem);
        }

        videoFrame = (FrameLayout) view.findViewById(R.id.map_video_frame);
        galleryFrame = (FrameLayout) view.findViewById(R.id.map_gallery_frame);

        recyclerViewLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        sceneLocationECRecyclerView = (RecyclerView) view.findViewById(R.id.map_associate_ecs_list);
        sceneLocationECRecyclerView.setLayoutManager(recyclerViewLayoutManager);
        sceneLocationECRecyclerView.setAdapter(new ECsAdapter());

        videoViewFragment = (ECVideoViewFragment) getChildFragmentManager().findFragmentById(R.id.ime_secene_location_video);
        galleryViewFragment = (ECGalleryViewFragment) getChildFragmentManager().findFragmentById(R.id.ime_secene_location_gallery);
    }

    @Override
    public void onDestroy(){
        if(mapView != null)
            mapView.onDestroy();
        super.onDestroy();

    }

    @Override
    public void onResume(){
        super.onResume();
        if(mapView != null)
            mapView.onResume();
    }

    @Override
    protected int getCloseButtonId(){
        return R.id.ime_map_close_button;
    }

    public void setLocationItem(String textTitle, final MovieMetaData.LocationItem locationItem){
        if (locationItem != null) {
            title = textTitle;
            selectedLocationItem = locationItem;
            if (ecTitle != null) {
                ecTitle.setText(textTitle);
            }
            if (mapView != null) {
                final LatLng location = new LatLng(selectedLocationItem.latitude, selectedLocationItem.longitude);

                List<MovieMetaData.LocationItem> locList = new ArrayList<MovieMetaData.LocationItem>();
                locList.add(locationItem);
                HttpImageHelper.getAllMapPins(locList, new ResultListener<Boolean>() {
                    @Override
                    public void onResult(Boolean result) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mapView.getMapAsync(new IMEOnMapReadyCallback(selectedLocationItem, true, null));
                            }
                        });
                    }

                    @Override
                    public <E extends Exception> void onException(E e) {

                    }
                });

            }

        }
    }

    class IMEOnMapReadyCallback implements OnMapReadyCallback{
        MovieMetaData.LocationItem locationItem;
        boolean isInteractive;
        GoogleMap.OnMapClickListener onMapClickListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

            }
        };

        public IMEOnMapReadyCallback(MovieMetaData.LocationItem locationItem, boolean isInteractive, GoogleMap.OnMapClickListener onMapClickListener){
            this.locationItem = locationItem;
            this.isInteractive = isInteractive;
            if (onMapClickListener != null){
                this.onMapClickListener = onMapClickListener;
            }
        }
        @Override
        public void onMapReady(final GoogleMap googleMap) {

            final LatLng location = new LatLng(locationItem.latitude, locationItem.longitude);

            if (!isInteractive){
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            }else
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

            googleMap.getUiSettings().setMapToolbarEnabled(false);


            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, locationItem.zoom));   // set location
            googleMap.getMaxZoomLevel();
            BitmapDescriptor bmDes ;
            if (selectedLocationItem.pinImage != null && !StringHelper.isEmpty(selectedLocationItem.pinImage.url)) {
                bmDes = BitmapDescriptorFactory.fromBitmap(HttpImageHelper.getMapPinBitmap(selectedLocationItem.pinImage.url));
            }else {
                bmDes = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
            }

            MarkerOptions markerOpt = new MarkerOptions()
                    .position(location).title(selectedLocationItem.getTitle()).snippet(selectedLocationItem.address)
                    .icon(bmDes);

            googleMap.addMarker(markerOpt).showInfoWindow();
            googleMap.setOnMapClickListener(onMapClickListener);
            googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
                @Override
                public void onCameraChange(CameraPosition camPos) {
                    if (camPos.zoom > locationItem.zoom && location != null) {
                        // set zoom 17 and disable zoom gestures so map can't be zoomed out
                        // all the way
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, locationItem.zoom));
                        googleMap.getUiSettings().setZoomGesturesEnabled(false);
                    }
                    if (camPos.zoom <= 17) {
                        googleMap.getUiSettings().setZoomGesturesEnabled(true);
                    }


                }
            });
        }
    }

    public class ECViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        CardView cv;
        ImageView locationPhoto;
        ImageView locationPlayIcon;
        MapView locationMapView;
        Object currentItem;
        int itemIndex;

        ECViewHolder(View itemView, Object item, int index) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            locationPhoto = (ImageView) itemView.findViewById(R.id.location_photo);
            locationPlayIcon = (ImageView) itemView.findViewById(R.id.location_play_image);
            locationMapView = (MapView) itemView.findViewById(R.id.ime_location_map_item);
            this.currentItem = item;
            itemIndex = index;
            itemView.setOnClickListener(this);
        }

        public void setItem(Object item, int position) {
            currentItem = item;
            itemIndex = position;
            if (item instanceof MovieMetaData.LocationItem) {
                MovieMetaData.LocationItem locationItem = ((MovieMetaData.LocationItem) item);
                locationPlayIcon.setVisibility(View.INVISIBLE);
                if (locationItem == null) { // TODO: CHECK
                    locationItem = ((MovieMetaData.LocationItem) item);
                }

                locationMapView.setVisibility(View.VISIBLE);
                locationPhoto.setVisibility(View.GONE);
                locationMapView.onCreate(savedInstanceState);
                locationMapView.setOnClickListener(null);
                locationMapView.getMapAsync(new IMEOnMapReadyCallback(locationItem, false, new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        onClick(null);
                    }
                }));
            } else if (item instanceof MovieMetaData.PresentationDataItem) {
                locationMapView.setVisibility(View.GONE);
                locationPhoto.setVisibility(View.VISIBLE);
                if (item instanceof MovieMetaData.AudioVisualItem)
                    locationPlayIcon.setVisibility(View.VISIBLE);
                else
                    locationPlayIcon.setVisibility(View.INVISIBLE);
                Glide.with(getActivity()).load(((MovieMetaData.PresentationDataItem) item).getPosterImgUrl()).centerCrop().into(locationPhoto);
            }
        }

        @Override
        public void onClick(View v) {
            if (currentItem != null) {
                if (currentItem instanceof MovieMetaData.LocationItem) {
                    if (videoViewFragment != null){
                        videoViewFragment.stopPlayback();
                    }
                    mapView.setVisibility(View.VISIBLE);
                    videoFrame.setVisibility(View.GONE);
                    galleryFrame.setVisibility(View.GONE);

                } else if (currentItem instanceof MovieMetaData.AudioVisualItem) {
                    mapView.setVisibility(View.GONE);
                    videoFrame.setVisibility(View.VISIBLE);
                    galleryFrame.setVisibility(View.GONE);

                    if (videoViewFragment == null)
                        videoViewFragment = (ECVideoViewFragment) getChildFragmentManager().findFragmentById(R.id.ime_secene_location_video);
                    videoViewFragment.setShouldAutoPlay(true);
                    videoViewFragment.setShouldHideMetaData(true);
                    videoViewFragment.setShouldShowCloseBtn(false);
                    videoViewFragment.setAspectRatioFramePriority(FixedAspectRatioFrameLayout.Priority.HEIGHT_PRIORITY);
                    videoViewFragment.setAudioVisualItem((MovieMetaData.AudioVisualItem) currentItem);


                } else if (currentItem instanceof MovieMetaData.ECGalleryItem) {
                    mapView.setVisibility(View.GONE);
                    videoFrame.setVisibility(View.GONE);
                    galleryFrame.setVisibility(View.VISIBLE);

                    if (videoViewFragment != null){
                        videoViewFragment.stopPlayback();
                    }
                    if (galleryViewFragment == null)
                        galleryViewFragment = (ECGalleryViewFragment) getChildFragmentManager().findFragmentById(R.id.ime_secene_location_gallery);
                    galleryViewFragment.setShouldHideMetaData(true);
                    galleryViewFragment.setShouldShowCloseBtn(false);
                    galleryViewFragment.setAspectRatioFramePriority(FixedAspectRatioFrameLayout.Priority.HEIGHT_PRIORITY);

                    galleryViewFragment.setCurrentGallery((MovieMetaData.ECGalleryItem) currentItem);
                }
            }
        }
    }

    public class ECsAdapter extends RecyclerView.Adapter<ECViewHolder> {


        int lastloadingIndex = -1;
        static final int PAGEITEMCOUNT = 6;


        public void reset() {
            lastloadingIndex = -1;
        }

        @Override
        public ECViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ime_locations_card_view, viewGroup, false);
            ECViewHolder pvh = new ECViewHolder(v, null, i);
            return pvh;
        }

        public void onBindViewHolder(ECViewHolder holder, int position) {

            if (selectedLocationItem != null) {
                if (position == 0){
                    holder.setItem(selectedLocationItem, 0);
                }else{
                    holder.setItem(selectedLocationItem.getPresentationDataItems().get(position - 1), position);
                }
            }

        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public int getItemCount() {
            if (selectedLocationItem != null){
                return selectedLocationItem.getPresentationDataItems() != null ? 1 + selectedLocationItem.getPresentationDataItems().size() : 1;
            }else
                return 0;
        }

    }

}
