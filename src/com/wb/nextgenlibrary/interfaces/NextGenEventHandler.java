package com.wb.nextgenlibrary.interfaces;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by stomata on 8/22/16.
 */
public interface NextGenEventHandler {
    void handleMovieTitleSelection(Activity activity, String movieId);
    boolean isDebugBuild();
    void userEventLog(String screen, String subScreen, String button, String action, String value);
    void handleShareLink(Activity activity, Fragment fragment, String shareUrl);
    boolean shouldShowInterstitialForContent(Object content);       // the default should be true so that Nextgen will show the interstitial, set this to false then NextGen will not show interstitial for the given content.
    void setInterstitialWatchedForContent(Object content);
    void setInterstitialSkippedForContent(Object content);
    void handlePurchaseButtonPressed(Activity ngeActivity, Object contentObject);
}
