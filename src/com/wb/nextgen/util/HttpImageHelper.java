package com.wb.nextgen.util;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import com.bumptech.glide.Glide;
import com.wb.nextgen.NextGenApplication;
import com.wb.nextgen.R;
import com.wb.nextgen.data.MovieMetaData;
import com.wb.nextgen.model.SceneLocation;
import com.wb.nextgen.util.concurrent.ResultListener;
import com.wb.nextgen.util.concurrent.Worker;
import com.wb.nextgen.util.utils.F;
import com.wb.nextgen.util.utils.NextGenLogger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Size;

public class HttpImageHelper {

    /** Fetch and cache the image */
    public static Bitmap fetchImage(URL url) throws IOException {
        Bitmap bitmap = null;
        byte[] bitmapByteArray = HttpHelper.fetchUrlBytes(url, true, true, false);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inInputShareable = true;
        try {
        	bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length, options);
            //bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length);
        } catch (OutOfMemoryError err) {
        	NextGenLogger.e(F.TAG, "HttpImageHelper.fetchImage: OutOfMemoryError, url: " + url);
        }
        
        if (bitmap == null) {
            // System.gc();
            options.inSampleSize = 4;
            try {
            	bitmap = BitmapFactory.decodeByteArray(bitmapByteArray, 0, bitmapByteArray.length, options);
            } catch (OutOfMemoryError err) {
            	NextGenLogger.e(F.TAG, "HttpImageHelper.fetchImage: OutOfMemoryError, inSampleSize=4, url: " + url);
            }
        }
        return bitmap;
    }

    private static HashMap<String, Bitmap> pinHash = new HashMap<String, Bitmap>();
    public static void getAllMapPins(final List<MovieMetaData.LocationItem> locationItems, ResultListener<Boolean> l){
        Worker.execute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                for (MovieMetaData.LocationItem item : locationItems) {
                    if (!pinHash.containsKey(item.pinImage.url)) {
                        try {
                            Bitmap theBitmap = Glide.
                                    with(NextGenApplication.getContext()).
                                    load(item.pinImage.url).asBitmap().
                                    into(item.pinImage.width, item.pinImage.height). // Width and height
                                    get();
                            pinHash.put(item.pinImage.url, theBitmap);
                        } catch (Exception ex) {
                            NextGenLogger.e(F.TAG, ex.getLocalizedMessage());
                        }
                    }
                }


                return true;
            }
        }, l);
    }
    public static void getAllMapPinsBySceneLocation(final List<SceneLocation> sceneLocations, ResultListener<Boolean> l){
        Worker.execute(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                for (SceneLocation sceneLocation : sceneLocations) {
                    if (!pinHash.containsKey(sceneLocation.location.pinImage.url)) {
                        try {
                            Bitmap theBitmap = Glide.
                                    with(NextGenApplication.getContext()).
                                    load(sceneLocation.location.pinImage.url).asBitmap().
                                    into(sceneLocation.location.pinImage.width, sceneLocation.location.pinImage.height). // Width and height
                                    get();
                            pinHash.put(sceneLocation.location.pinImage.url, theBitmap);
                        } catch (Exception ex) {
                            NextGenLogger.e(F.TAG, ex.getLocalizedMessage());
                        }
                    }
                }


                return true;
            }
        }, l);
    }

    public static Bitmap getMapPinBitmap(String imageUrl){
        Bitmap theBitmap = null;
        if (pinHash.containsKey(imageUrl))
            theBitmap = pinHash.get(imageUrl);
        else{
            if (theBitmap == null){
                try{
                    String uri = NextGenUtils.getPacakageImageUrl(R.drawable.mos_map_pin);
                    theBitmap = MediaStore.Images.Media.getBitmap(NextGenApplication.getContext().getContentResolver(), Uri.parse(uri));
                }catch (Exception ex){
                    NextGenLogger.e(F.TAG, ex.getLocalizedMessage());
                }
            }
        }


        return theBitmap;
    }

    /** Fetch the image only (for analytics) */
    public static void fetchImageDoNotCache(URL url) throws IOException {
        HttpHelper.fetchUrlBytes(url, false, true, false);
    }
}
