package com.wb.nextgenlibrary.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.gson.Gson;
import com.wb.nextgenlibrary.NextGenExperience;
import com.wb.nextgenlibrary.R;

import com.wb.nextgenlibrary.activity.ActorGalleryActivity;
import com.wb.nextgenlibrary.adapter.ActorDetailGalleryRecyclerAdapter;
import com.wb.nextgenlibrary.analytic.NextGenAnalyticData;
import com.wb.nextgenlibrary.data.MovieMetaData;
import com.wb.nextgenlibrary.data.MovieMetaData.Filmography;
import com.wb.nextgenlibrary.data.MovieMetaData.CastData;
import com.wb.nextgenlibrary.network.BaselineApiDAO;
import com.wb.nextgenlibrary.util.DialogUtils;
import com.wb.nextgenlibrary.util.concurrent.ResultListener;
import com.wb.nextgenlibrary.util.utils.StringHelper;
import com.wb.nextgenlibrary.widget.FixedAspectRatioFrameLayout;

import java.util.List;

/**
 * Created by gzcheng on 1/13/16.
 */
public class NextGenActorDetailFragment extends AbstractNextGenFragment implements View.OnClickListener, ActorDetailGalleryRecyclerAdapter.ActorGalleryRecyclerSelectionListener{

    CastData actorOjbect;
    ImageView fullImageView;
    TextView detailTextView;
    TextView actorNameTextView;

    RecyclerView filmographyRecyclerView;
    LinearLayoutManager filmographyLayoutManager;
    ActorDetailFimograpyAdapter filmographyAdaptor;

    RecyclerView actorGalleryRecyclerView;
    LinearLayoutManager actorGalleryLayoutManager;
    ActorDetailGalleryRecyclerAdapter actorGalleryAdaptor;
    View actorGalleryFrame;

    ImageButton facebookBtn;
    ImageButton twitterBtn;
    ImageButton instagramBtn;

    int layoutId = R.layout.next_gen_actor_detail_view;

    boolean bEnableActorGallery = false;

    int getContentViewId(){
        return 0;
    }

    public void setLayoutId(int layoutId){
        this.layoutId = layoutId;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (bEnableActorGallery){

        }
        return inflater.inflate(layoutId, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fullImageView = (ImageView)view.findViewById(R.id.next_gen_detail_full_image);
        detailTextView = (TextView)view.findViewById(R.id.actor_biography_text);
        detailTextView.setMovementMethod(new ScrollingMovementMethod());
        actorNameTextView = (TextView)view.findViewById(R.id.actor_real_name_text);
        filmographyRecyclerView = (RecyclerView)view.findViewById(R.id.actor_detail_filmography);
        actorGalleryRecyclerView = (RecyclerView) view.findViewById(R.id.actor_gallery_recycler);
        actorGalleryFrame = view.findViewById(R.id.actor_gallery_recycler_frame);

        facebookBtn = (ImageButton)view.findViewById(R.id.actor_page_facebook_button);
        twitterBtn = (ImageButton)view.findViewById(R.id.actor_page_twitter_button);
        instagramBtn = (ImageButton)view.findViewById(R.id.actor_page_instagram_button);
        if (facebookBtn != null){
            facebookBtn.setOnClickListener(this);
        }
        if (twitterBtn != null){
            twitterBtn.setOnClickListener(this);
        }
        if (instagramBtn != null){
            instagramBtn.setOnClickListener(this);
        }

        if (filmographyRecyclerView != null){
            filmographyLayoutManager = new GridLayoutManager(getActivity(), 1, GridLayoutManager.HORIZONTAL, false);
            filmographyRecyclerView.setLayoutManager(filmographyLayoutManager);
            filmographyAdaptor = new ActorDetailFimograpyAdapter();
            filmographyRecyclerView.setAdapter(filmographyAdaptor);
        }

        if (actorGalleryRecyclerView != null && bEnableActorGallery){
            setbEnableActorGallery(bEnableActorGallery);
        } else if (actorGalleryFrame != null){
            actorGalleryFrame.setVisibility(View.GONE);
        }
        CastData actor = actorOjbect;
        actorOjbect = null;
        reloadDetail(actor);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (filmographyRecyclerView != null)
            filmographyRecyclerView.setAdapter(null);
        if (actorGalleryRecyclerView != null)
            actorGalleryRecyclerView.setAdapter(null);
    }

    @Override
    String getReportContentName(){
        if (actorOjbect != null)
            return actorOjbect.displayName;
        else
            return "";
    }

    public void setDetailObject(CastData object){
        actorOjbect = object;
    }

    public void setbEnableActorGallery(boolean enable){
        bEnableActorGallery = enable;
        if (actorGalleryRecyclerView != null && bEnableActorGallery && actorGalleryAdaptor == null){
            actorGalleryLayoutManager = new GridLayoutManager(getActivity(), 1, GridLayoutManager.HORIZONTAL, false);
            actorGalleryRecyclerView.setLayoutManager(actorGalleryLayoutManager);
            actorGalleryAdaptor = new ActorDetailGalleryRecyclerAdapter(getActivity(), this);
            actorGalleryRecyclerView.setAdapter(actorGalleryAdaptor);
            actorGalleryFrame.setVisibility(View.VISIBLE);
        }
    }

    public void reloadDetail(final CastData object){

        if (object != null && object.getBaselineCastData() != null && !object.equals(actorOjbect)){
            actorOjbect = object;
            detailTextView.scrollTo(0,0);
            Glide.with(getActivity()).load(actorOjbect.getBaselineCastData().getFullImageUrl()).centerCrop().into(fullImageView);

            fullImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (object.getBaselineCastData() != null && object.getBaselineCastData().headShots != null && object.getBaselineCastData().headShots.size()> 0) {
                        onItemSelected(-1);     // pass -1 because we set index + 1 in onItemSelected
                        NextGenAnalyticData.reportEvent(getActivity(), NextGenActorDetailFragment.this, "Actor Thumbnail Image",
                                NextGenAnalyticData.AnalyticAction.ACTION_CLICK, object.getBaselineCastData().headShots.get(0).fullSizeUrl);

                    }
                }
            });
            if (actorOjbect.getBaselineCastData().filmogrphies == null){
                BaselineApiDAO.getFilmographyAndBioOfPerson(actorOjbect.getBaselineActorId(), new ResultListener<MovieMetaData.BaselineCastData>() {
                    @Override
                    public void onResult(MovieMetaData.BaselineCastData result) {
                        actorOjbect.getBaselineCastData().filmogrphies = result.filmogrphies;
                        actorOjbect.getBaselineCastData().biography = result.biography;
                        if (getActivity() != null) {
                            synchronized (getActivity()) {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        detailTextView.setText(actorOjbect.getBaselineCastData().biography);
                                        updateFilmographyList();
                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public <E extends Exception> void onException(E e) {

                    }
                });
            }else{
                updateFilmographyList();
            }

            String facebookUrl = actorOjbect.getBaselineCastData().getSocialMediaUrl(MovieMetaData.BaselineCastData.SOCIAL_MEDIA_KEY.FACEBOOK_KEY);
            if (!StringHelper.isEmpty(facebookUrl)){
                facebookBtn.setVisibility(View.VISIBLE);
            }else
                facebookBtn.setVisibility(View.GONE);

            String instagramUrl = actorOjbect.getBaselineCastData().getSocialMediaUrl(MovieMetaData.BaselineCastData.SOCIAL_MEDIA_KEY.INSTAGRAM_KEY);
            if (!StringHelper.isEmpty(instagramUrl)){
                instagramBtn.setVisibility(View.VISIBLE);
            }else
                instagramBtn.setVisibility(View.GONE);

            String twitterUrl = actorOjbect.getBaselineCastData().getSocialMediaUrl(MovieMetaData.BaselineCastData.SOCIAL_MEDIA_KEY.TWITTER_KEY);
            if (!StringHelper.isEmpty(twitterUrl)){
                twitterBtn.setVisibility(View.VISIBLE);
            }else
                twitterBtn.setVisibility(View.GONE);

            if (actorOjbect.getBaselineCastData().getGallery() != null && bEnableActorGallery && actorGalleryAdaptor != null){
                List<MovieMetaData.CastHeadShot> headShots = actorOjbect.getBaselineCastData().getGallery();
                if (headShots != null && headShots.size() > 1)
                    actorGalleryAdaptor.setCastHeadShots(headShots.subList(1, headShots.size()));
                else{
                    actorGalleryAdaptor.setCastHeadShots(null);
                }
                actorGalleryAdaptor.notifyDataSetChanged();
            }

            detailTextView.setText(actorOjbect.getBaselineCastData().biography);
            actorNameTextView.setText(actorOjbect.displayName.toUpperCase());
        }
    }

    private void updateFilmographyList(){
        final List<Filmography> updateList;
        if (actorOjbect.getBaselineCastData() == null) {
            updateList = null;
        }else if ( actorOjbect.getBaselineCastData().filmogrphies != null && actorOjbect.getBaselineCastData().filmogrphies.size() > 0) {
            updateList = actorOjbect.getBaselineCastData().filmogrphies;
        }else {
            updateList = null;      //clear the list

        }

        if (getActivity() != null) {
            filmographyAdaptor.setFilmographies(updateList);
            filmographyAdaptor.notifyDataSetChanged();
        }


    }

    public class FilmographyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        CardView cv;
        ImageView personPhoto;
        Filmography filmInfo;
        FixedAspectRatioFrameLayout imageFrame;

        FilmographyViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            personPhoto = (ImageView)itemView.findViewById(R.id.person_photo);
            imageFrame = (FixedAspectRatioFrameLayout)itemView.findViewById(R.id.actor_detail_filmography_frame_layout);
            itemView.setOnClickListener(this);
        }

        public void setFilmInfo(Filmography filmInfo, int position){
            this.filmInfo = filmInfo;
            if (filmInfo.isFilmPosterRequest()) {
               // Glide.with(getActivity()).load(filmInfo.getFilmPosterImageUrl()).asBitmap().fitCenter().into(personPhoto);
                Glide.with(getActivity()).load(filmInfo.getFilmPosterImageUrl()).listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(final GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageFrame.setAspectRatio(resource.getIntrinsicWidth(), resource.getIntrinsicHeight());
                            }
                        });
                        return false;
                    }
                }).fitCenter().into(personPhoto);
                //NextGenLogger.d(F.TAG, "Position: " + position  +" loaded: " + filmInfo.getFilmPosterImageUrl());
            }
        }

        @Override
        public void onClick(View v) {
            if (NextGenExperience.getNextGenEventHandler() != null){
                NextGenExperience.getNextGenEventHandler().handleMovieTitleSelection(getActivity(), filmInfo.title);
            }
            NextGenAnalyticData.reportEvent(getActivity(), NextGenActorDetailFragment.this, "Actor Filmography",
                    NextGenAnalyticData.AnalyticAction.ACTION_CLICK, filmInfo.title);

        }
    }

    public class ActorDetailFimograpyAdapter extends RecyclerView.Adapter<FilmographyViewHolder>{

        List<Filmography> filmographies = null;

        public void setFilmographies(List<Filmography> filmographies){
            this.filmographies = filmographies;
        }

        @Override
        public FilmographyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.actor_filmography_cardview, viewGroup, false);
            FilmographyViewHolder pvh = new FilmographyViewHolder(v);
            return pvh;
        }

        public void onBindViewHolder(FilmographyViewHolder holder, int position){


            if (filmographies != null && filmographies.size() > position) {
                Filmography film = filmographies.get(position);
                holder.setFilmInfo(film, position);
            }

        }

        public int getItemCount(){
            if (filmographies != null )
                return filmographies.size();
            else
                return 0;
        }

    }

    public CastData getDetailObject(){
        return actorOjbect;
    }


    @Override
    public void onItemSelected(int index){
        Intent actorGalleryIntent = new Intent(getActivity(), ActorGalleryActivity.class);
        String actorGallery = (new Gson()).toJson(actorOjbect.getBaselineCastData().getGallery());
        actorGalleryIntent.putExtra(ActorGalleryActivity.HEAD_SHOTS_KEY, actorGallery);
        actorGalleryIntent.putExtra(ActorGalleryActivity.CURRENT_INDEX_KEY, index + 1);

        startActivity(actorGalleryIntent);
    }

    @Override
    public void onClick(View v){
        String socialUrl = null;
        if (v.equals(facebookBtn)){
            socialUrl = actorOjbect.getBaselineCastData().getSocialMediaUrl(MovieMetaData.BaselineCastData.SOCIAL_MEDIA_KEY.FACEBOOK_KEY);

            try {
                getActivity().getPackageManager().getPackageInfo("com.facebook.katana", 0);
                // http://stackoverflow.com/a/24547437/1048340
                socialUrl = "fb://facewebmodal/f?href=" + socialUrl;
                NextGenAnalyticData.reportEvent(getActivity(), this, "Facebook Button",
                        NextGenAnalyticData.AnalyticAction.ACTION_CLICK, socialUrl);
            } catch (PackageManager.NameNotFoundException e) {

            }

        }else if (v.equals(twitterBtn)){
            socialUrl = actorOjbect.getBaselineCastData().getSocialMediaUrl(MovieMetaData.BaselineCastData.SOCIAL_MEDIA_KEY.TWITTER_KEY);
            NextGenAnalyticData.reportEvent(getActivity(), this, "Twitter Button",
                    NextGenAnalyticData.AnalyticAction.ACTION_CLICK, socialUrl);
        }else if (v.equals(instagramBtn)){
            socialUrl = actorOjbect.getBaselineCastData().getSocialMediaUrl(MovieMetaData.BaselineCastData.SOCIAL_MEDIA_KEY.INSTAGRAM_KEY);
            NextGenAnalyticData.reportEvent(getActivity(), this, "Instagram Button",
                    NextGenAnalyticData.AnalyticAction.ACTION_CLICK, socialUrl);
        }

        if (!StringHelper.isEmpty(socialUrl)){
            final String targetUrl = socialUrl;
            DialogUtils.showLeavingAppDialog(getActivity(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    NextGenExperience.launchChromeWithUrl(targetUrl);
                }
            });
        }
    }

}
