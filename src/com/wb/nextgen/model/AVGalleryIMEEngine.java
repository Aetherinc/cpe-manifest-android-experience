package com.wb.nextgen.model;

import com.wb.nextgen.data.MovieMetaData;
import com.wb.nextgen.data.MovieMetaData.PresentationDataItem;
import com.wb.nextgen.data.MovieMetaData.IMEElement;
import com.wb.nextgen.util.utils.StringHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gzcheng on 4/12/16.
 */
public class AVGalleryIMEEngine extends NextGenIMEEngine<IMEElement<PresentationDataItem>> {

    public AVGalleryIMEEngine(List<IMEElement<PresentationDataItem>> elements){
        imeElements = elements;
    }

    public int compareCurrentTimeWithItemAtIndex(long timecode, int index){
        return imeElements.get(index).compareTimeCode(timecode);
    }


    // returns true if there is an update of the current item
    private IMEElement<PresentationDataItem> computeMultipleCurrentIMEElements(){
        List<PresentationDataItem> listOfItems = new ArrayList<PresentationDataItem>();

        if (currentIMEItem != null){
            listOfItems.add(currentIMEItem.imeObject);
            if (currentIndex != imeElements.size()-1 ) {
                for (int i = currentIndex + 1; i < imeElements.size(); i++) { //look ahead
                    if (compareCurrentTimeWithItemAtIndex(lastSearchedTime, i) == 0) {
                        listOfItems.add(imeElements.get(i).imeObject);
                    } else
                        break;
                }
            }
            if (currentIndex != 0) {
                for (int i = currentIndex - 1; i >= 0; i--) { //look before
                    if (compareCurrentTimeWithItemAtIndex(lastSearchedTime, i) == 0) {
                        listOfItems.add(imeElements.get(i).imeObject);
                    } else
                        break;

                }
            }
            if (listOfItems.size() == 1){
                return currentIMEItem;
            }else{
                return new IMEElement(currentIMEItem.startTimecode, currentIMEItem.endTimecode, new IMECombineItem(listOfItems));
            }
        }

        return currentIMEItem;


    }

    public IMEElement<PresentationDataItem> getCurrentIMEElement(){
        return computeMultipleCurrentIMEElements();
    }

    public static class IMECombineItem extends PresentationDataItem{
        List<PresentationDataItem> items;
        MovieMetaData.PictureItem pictureItem;
        MovieMetaData.TextItem textItem;
        boolean isLocation = false;
        public IMECombineItem(List<PresentationDataItem> items){
            super(null, null);
            this.items = items;
            for(PresentationDataItem pItems : items){
                if (pItems instanceof MovieMetaData.PictureItem){
                    this.posterImgUrl = ((MovieMetaData.PictureItem)pItems).thumbnail.url;
                    pictureItem = ((MovieMetaData.PictureItem)pItems);
                } else if (pItems instanceof MovieMetaData.TextItem){
                    this.title = ((MovieMetaData.TextItem)pItems).getTitle();
                    textItem = ((MovieMetaData.TextItem)pItems);
                } else if (pItems instanceof MovieMetaData.LocationItem){
                    isLocation = true;
                }
            }

            if (StringHelper.isEmpty(title)){
                if (items != null && items.size() > 0 ){
                    title = items.get(0).getTitle();
                }
            }
        }

        public String getPosterImgUrl(){
            if (StringHelper.isEmpty(posterImgUrl)){
                if (items != null && items.size() > 0 ){
                    posterImgUrl = items.get(0).getPosterImgUrl();
                }
            }
            return posterImgUrl;
        }

        public MovieMetaData.TextItem getTextItem(){
            return textItem;
        }

        public MovieMetaData.PictureItem getPictureItem(){
            return  pictureItem;
        }

        public boolean isLocation() {
            return isLocation;
        }

        public List<PresentationDataItem> getAllPresentationItems(){
            return items;
        }
    }


}
