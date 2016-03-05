package com.wb.nextgen.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wb.nextgen.R;

import com.wb.nextgen.data.DemoJSONData.ActorInfo;
import com.wb.nextgen.data.DemoJSONData.Filmography;
import com.wb.nextgen.util.PicassoTrustAll;

import org.w3c.dom.Text;

/**
 * Created by gzcheng on 1/13/16.
 */
public class NextGenActorDetailFragment extends Fragment{

    ActorInfo actorOjbect;
    ImageView fullImageView;
    TextView detailTextView;
    TextView actorNameTextView;
    TextView characterTextView;
    RecyclerView filmographyRecyclerView;
    LinearLayoutManager filmographyLayoutManager;
    ActorDetailFimograpyAdapter filmographyAdaptor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.next_gen_actor_detail_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fullImageView = (ImageView)view.findViewById(R.id.next_gen_detail_full_image);
        detailTextView = (TextView)view.findViewById(R.id.actor_biography_text);
        characterTextView = (TextView)view.findViewById(R.id.actor_character_name_text);
        actorNameTextView = (TextView)view.findViewById(R.id.actor_real_name_text);
        filmographyRecyclerView = (RecyclerView)view.findViewById(R.id.actor_detail_filmography);

        if (filmographyRecyclerView != null){
            filmographyLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            filmographyRecyclerView.setLayoutManager(filmographyLayoutManager);
            filmographyAdaptor = new ActorDetailFimograpyAdapter();
            filmographyRecyclerView.setAdapter(filmographyAdaptor);
        }
        reloadDetail(actorOjbect);
    }

    public void setDetailObject(ActorInfo object){
        actorOjbect = object;
    }

    public void reloadDetail(ActorInfo object){
        actorOjbect = object;
        if (actorOjbect != null){
            PicassoTrustAll.loadImageIntoView(getActivity(), actorOjbect.getFullImageUri(), fullImageView);
            detailTextView.setText(actorOjbect.biography);
            characterTextView.setText(actorOjbect.character);
            actorNameTextView.setText(actorOjbect.realName);
            filmographyAdaptor.notifyDataSetChanged();
        }
    }

    public static class FilmographyViewHolder extends RecyclerView.ViewHolder {
        CardView cv;
        TextView personName;
        TextView personAge;
        ImageView personPhoto;

        FilmographyViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            personName = (TextView)itemView.findViewById(R.id.person_name);
            personAge = (TextView)itemView.findViewById(R.id.person_age);
            personPhoto = (ImageView)itemView.findViewById(R.id.person_photo);
        }
    }

    public class ActorDetailFimograpyAdapter extends RecyclerView.Adapter<FilmographyViewHolder>{

        @Override
        public FilmographyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.actor_filmography_cardview, viewGroup, false);
            FilmographyViewHolder pvh = new FilmographyViewHolder(v);
            return pvh;
        }
        public void onBindViewHolder(FilmographyViewHolder holder, int position){
            holder.personName.setText(actorOjbect.actorFilmography[position].title);
            holder.personAge.setText(actorOjbect.actorFilmography[position].title);
            PicassoTrustAll.loadImageIntoView(getActivity(), actorOjbect.actorFilmography[position].posterImageUrl, holder.personPhoto);
            //holder.personPhoto.setImageResource(persons.get(i).photoId);
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public int getItemCount(){
            return actorOjbect.actorFilmography.length;
        }

    }

    public ActorInfo getDetailObject(){
        return actorOjbect;
    }

}
