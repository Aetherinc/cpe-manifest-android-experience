package com.wb.nextgenlibrary;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.wb.cpedata.CPEDataParser;
import com.wb.cpedata.ManifestItem;
import com.wb.cpedata.data.manifest.CPEData;
import com.wb.nextgenlibrary.activity.LauncherActivity;
import com.wb.nextgenlibrary.fragment.AbstractCastMainMovieFragment;
import com.wb.nextgenlibrary.fragment.AbstractNGEMainMovieFragment;
import com.wb.nextgenlibrary.interfaces.NGEEventHandler;
import com.wb.nextgenlibrary.network.NGECacheManager;
import com.wb.cpedata.util.Size;
import com.wb.nextgenlibrary.util.utils.F;
import com.wb.nextgenlibrary.util.utils.NextGenLogger;
import com.wb.nextgenlibrary.util.utils.StringHelper;

import java.util.List;
import java.util.Locale;

/**
 * Created by gzcheng on 8/10/16.
 */
public class NextGenExperience {

    private static Context applicationContext;
    private static CPEData movieMetaData;
    private static float deviceScreenDensity = 0.0f;
    private static int deviceScreenWidth = -1;
    private static int deviceScreenHeight = -1;
    private static Size deviceScreenSize = null;
    public static int sCachePolicy = NGECacheManager.POLICY_MEDIUM;
    public static NGECacheManager sCacheManager;
    private static String sVersionName = null;
    private static Locale clientLocale = null;
    private static boolean isDiagnosticMode = true;
    private static Class<? extends AbstractNGEMainMovieFragment> mainMovieFragmentClass;
    private static Class<? extends AbstractCastMainMovieFragment> castMovieFragmentClass;
    private static Object nextgenPlaybackObject;
    private static NGEEventHandler NGEEventHandler;
    private static String googleMapAPIKey = null;
    private static ManifestItem manifestItem = null;
    private static String studioXAPIKey = null;

    private static String sUserAgent;

    public static void exitExperience(){
        movieMetaData = null;
        nextgenPlaybackObject = null;
        applicationContext = null;
        mainMovieFragmentClass = null;
        castMovieFragmentClass = null;
        NGEEventHandler = null;
        clientLocale = null;
        manifestItem = null;
        studioXAPIKey = null;

        sUserAgent = null;
    }

    public static void startNextGenExperience(Context appContext, final Activity launcherActivity, final ManifestItem item,
                                              Object playbackObject, Class<? extends AbstractNGEMainMovieFragment> fragmentClass,
                                              Class<? extends AbstractCastMainMovieFragment> castFragmentClass,
                                              NGEEventHandler eventHandler, @NonNull String studioStr) throws NextGenEmptyStudioStringException{
        startNextGenExperience(appContext, launcherActivity, item, playbackObject, fragmentClass, castFragmentClass, eventHandler, null, studioStr);
    }

    public static void startNextGenExperience(Context appContext, final Activity launcherActivity, final ManifestItem item,
                                              Object playbackObject, Class<? extends AbstractNGEMainMovieFragment> fragmentClass,
                                              Class<? extends AbstractCastMainMovieFragment> castFragmentClass,
                                              NGEEventHandler eventHandler, Locale locale, @NonNull String studioStr) throws NextGenEmptyStudioStringException{

        if (StringHelper.isEmpty(studioStr))
            throw new NextGenEmptyStudioStringException();

        nextgenPlaybackObject = playbackObject;

        applicationContext = appContext;
        if (sCacheManager == null)
            sCacheManager = new NGECacheManager(applicationContext);
        mainMovieFragmentClass = fragmentClass;
        castMovieFragmentClass = castFragmentClass;
        NGEEventHandler = eventHandler;
        clientLocale = locale == null? Locale.getDefault() : locale;
        manifestItem = item;
        studioXAPIKey = studioStr;

        sUserAgent = "Android/" + sVersionName + " (Linux; U; Android " + Build.VERSION.RELEASE + "; " + Build.MODEL + ")";
        try {
            ApplicationInfo appInfo = appContext.getPackageManager().getApplicationInfo(
                    appContext.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData != null) {
                googleMapAPIKey = appInfo.metaData.getString("com.google.android.geo.API_KEY");
            }
        } catch (PackageManager.NameNotFoundException e) {
        }

        Intent intent = new Intent(launcherActivity, LauncherActivity.class);
        launcherActivity.startActivity(intent);
    }

    public static class NextGenEmptyStudioStringException extends Exception{
        @Override
        public String getMessage(){
            return "Studio string cannot be empty";
        }
    }

    public static String getStudioXAPIKey(){
        return studioXAPIKey;
    }

    public static boolean startNextGenParsing(ManifestItem manifestItem, Locale locale) {
		try {
			// install security provider before calling getCastActorsData to avoid SSL errors on < Android 5.0 devices
			ProviderInstaller.installIfNeeded(getApplicationContext());
		} catch (GooglePlayServicesRepairableException ex) {
			// Indicates that Google Play services is out of date, disabled, etc.
			// Prompt the user to install/update/enable Google Play services.
			GooglePlayServicesUtil.showErrorNotification(
					ex.getConnectionStatusCode(), getApplicationContext());
		} catch (GooglePlayServicesNotAvailableException ex) {
			NextGenLogger.e(F.TAG, "GooglePlayServicesNotAvailableException: " + ex.getMessage());
		}

		movieMetaData = CPEDataParser.parseCPEManifests(applicationContext, manifestItem, locale);
		return movieMetaData != null;
    }

    public static ManifestItem getManifestItem(){
        return manifestItem;
    }

    public static Context getApplicationContext(){
        return applicationContext;
    }

    public static CPEData getCPEData(){
        return movieMetaData;
    }

    public static Class<? extends AbstractNGEMainMovieFragment> getMainMovieFragmentClass(){
        return mainMovieFragmentClass;
    }

    public static Class<? extends AbstractCastMainMovieFragment> getCastMovieFragmentClass(){
        return castMovieFragmentClass;
    }

    public static Object getNextgenPlaybackObject(){
        return nextgenPlaybackObject;
    }

    public static NGEEventHandler getNextGenEventHandler() {
        return NGEEventHandler;
    }

    public static float getScreenDensity(Context ctx) {
        if (deviceScreenDensity == 0.0f && ctx != null)
            deviceScreenDensity = ctx.getResources().getDisplayMetrics().density;
        return deviceScreenDensity;
    }

    public static int getScreenWidth(Context ctx){
        if (deviceScreenWidth == -1 && ctx != null){
            getScreenSize(ctx);
        }
        return deviceScreenWidth;
    }

    public static int getScreenHeight(Context ctx){
        if (deviceScreenHeight == -1 && ctx != null){
            getScreenSize(ctx);
        }
        return deviceScreenHeight;
    }

    public static Size getScreenSize(Context ctx){
        if (deviceScreenSize == null && ctx != null){
            WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getRealMetrics(metrics);
            deviceScreenHeight = metrics.heightPixels;
            deviceScreenWidth = metrics.widthPixels;
            deviceScreenSize = new Size(metrics.widthPixels, metrics.heightPixels);
        }
        return deviceScreenSize;
    }

    public static int getCachePolicy() {
        return sCachePolicy;
    }

    public static String getUserAgent() {
        return sUserAgent;
    }

    public static void launchChromeWithUrl(String urlString){
        if (StringHelper.isEmpty(urlString))
            return;

        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setData(Uri.parse(urlString));

        try {
            applicationContext.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            intent.setPackage(null);
            applicationContext.startActivity(intent);
        }
    }

    public static void launchSocialSharingWithUrl(Activity activity, String sharingString){
        try {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, sharingString);

            activity.startActivity(Intent.createChooser(sharingIntent, "Share using"));
        }catch (Exception ex){
            NextGenLogger.e(F.TAG, ex.getLocalizedMessage());
        }
    }

    public static Locale getClientLocale(){
        return clientLocale;
    }

    public static boolean isDebugBuild() {
        return true;
    }



    public static boolean isDiagnosticMode() {
        return isDiagnosticMode;
    }

    public static void enableDiagnosticMode() {
        isDiagnosticMode = true;
        //applicationContext.setSharedPrefs(PREFS_DIAGNOSTIC_MODE + sDay, true);
    }

    public static String getGoogleMapAPIKey(){
        return googleMapAPIKey;
    }

    public static boolean matchesClientLocale(String languageLocale){
        if (languageLocale != null){
            languageLocale = languageLocale.replace("-", "_");
            if (languageLocale.contains("_")){  // this is locale
                return clientLocale.toString().equals(languageLocale);
            }else{      // this is language
                return clientLocale.getLanguage().equals(languageLocale);
            }
        }
        return false;
    }
}
