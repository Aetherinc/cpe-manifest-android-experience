package com.wb.nextgen;

import android.app.Application;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.wb.nextgen.activity.StartupActivity;
import com.wb.nextgen.data.DemoData;
import com.wb.nextgen.data.MovieMetaData;
import com.wb.nextgen.network.BaselineApiDAO;
import com.wb.nextgen.network.FlixsterCacheManager;
import com.wb.nextgen.network.TheTakeApiDAO;
import com.wb.nextgen.parser.ManifestXMLParser;
import com.wb.nextgen.model.NextGenSettings;
import com.wb.nextgen.parser.manifest.schema.v1_4.MediaManifestType;
import com.wb.nextgen.util.utils.F;
import com.wb.nextgen.util.utils.NextGenLogger;


import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import javax.security.auth.x500.X500Principal;

/**
 * Created by gzcheng on 1/13/16.
 */
public class NextGenApplication extends Application {
    private static boolean isDiagnosticMode = true;
    public static final String PREFS_NAME = "NextGenPrefs";
    private static final String PREFS_CACHEPOLICY = "PREFS_CACHEPOLICY";
    // //////////////////////////////////////////////
    // Certificate
    private static boolean debugCertExists;
    private static boolean rcCertExists;
    //public static boolean prodCert;
    private static final X500Principal DEBUG_DN = new X500Principal("CN=Android Debug,O=Android,C=US");
    private static final long RC_CERT_SERIAL_NUMBER = 0x52265208;
    private static final String PREFS_DIAGNOSTIC_MODE = "PREFS_DIAGNOSTIC_MODE";

    private static SharedPreferences sSettings;
    private static SharedPreferences.Editor sEditor;
    public static Date sToday;
    public static int sDay;
    private static Context sApplicationContext;
    private static float deviceScreenDensity = 0.0f;
    private static int deviceScreenWidth = -1;
    private static int deviceScreenHeight = -1;
    public static int sCachePolicy;
    private static String sUserAgent = null;
    private static String sVersionName = null;
    private static int sVersionCode = 0;
    private static Locale locale;
    private static String sCountryCode = null;
    private static String sClientCountryCode = null;
    private static String sClientLanguageCode = null;
    private static PackageInfo sNextGenInfo = null;

    private static NextGenSettings sAppSettings;

    private static MovieMetaData movieMetaData;
    // ///////////////////////////////////////////////////////
    // Flixster Cache

    public static FlixsterCacheManager sFlixsterCacheManager;

    @SuppressWarnings("unchecked")
    @Override
    public void onCreate() {
        super.onCreate();
        try {

            sSettings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            sDay = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
            sApplicationContext = this.getApplicationContext();
            sFlixsterCacheManager = new FlixsterCacheManager(this);
            sCachePolicy = sSettings.getInt(PREFS_CACHEPOLICY, FlixsterCacheManager.POLICY_MEDIUM);
            /// Construct user Agent string
            sNextGenInfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            //setLocalization();

            sAppSettings = new NextGenSettings();

            sVersionName = sNextGenInfo.versionName;
            sVersionCode = sNextGenInfo.versionCode;
            sUserAgent = "Android/" + sVersionName + " (Linux; U; Android " + Build.VERSION.RELEASE + "; " + locale
                    + "; " + Build.MODEL + ")";
            DemoData.parseDemoJSONData();

            NextGenLogger.d("TIME_THIS", "---------------Next Test--------------");

           //startNextGenExperience();

        }catch (Exception ex){
            NextGenLogger.e(F.TAG, ex.getLocalizedMessage());
            NextGenLogger.e(F.TAG, ex.getStackTrace().toString());
        }
    }

    public static boolean startNextGenExperience(StartupActivity.ManifestItem manifestItem){
        try{
            NextGenLogger.d("TIME_THIS", "---------------Next Test--------------");

            long systime = SystemClock.currentThreadTimeMillis();
            ManifestXMLParser.NextGenManifestData manifest = new ManifestXMLParser().startParsing(manifestItem.manifestFileUrl, manifestItem.appDataFileUrl);
            long currentTime = SystemClock.currentThreadTimeMillis() - systime;
            NextGenLogger.d("TIME_THIS", "Time to finish parsing: " + currentTime);
            movieMetaData = MovieMetaData.process(manifest);


            currentTime = SystemClock.currentThreadTimeMillis() - currentTime;
            NextGenLogger.d("TIME_THIS", "Time to finish processing: " + currentTime);

            BaselineApiDAO.init();
            TheTakeApiDAO.init();
            return true;
        }catch (Exception ex){
            NextGenLogger.e(F.TAG, ex.getLocalizedMessage());
            NextGenLogger.e(F.TAG, ex.getStackTrace().toString());
            return false;
        }
    }

    public static MovieMetaData getMovieMetaData(){
        return movieMetaData;
    }

    /** @return The application context */
    public static Context getContext() {
        return sApplicationContext;
    }

    public static void checkSigningCert(Context ctx) {
        try {
            PackageInfo pinfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_SIGNATURES);
            Signature signatures[] = pinfo.signatures;

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            for (int i = 0; i < signatures.length; i++) {
                ByteArrayInputStream stream = new ByteArrayInputStream(signatures[i].toByteArray());
                X509Certificate cert = (X509Certificate) cf.generateCertificate(stream);
                debugCertExists = cert.getSubjectX500Principal().equals(DEBUG_DN);
                rcCertExists = cert.getSerialNumber().longValue() == RC_CERT_SERIAL_NUMBER;
            }
        } catch (PackageManager.NameNotFoundException e) {
        } catch (CertificateException e) {
        }
    }

    public static boolean isDebugBuild() {
        return true;//debugCertExists;
    }

    public static boolean isRcBuild() {
        return rcCertExists;
    }

    public static boolean isDiagnosticMode() {
        return isDiagnosticMode;
    }

    public static void enableDiagnosticMode() {
        isDiagnosticMode = true;
        setSharedPrefs(PREFS_DIAGNOSTIC_MODE + sDay, true);
    }

    private static void setSharedPrefs(String key, String value) {
        sEditor.putString(key, value);
        sEditor.commit();
    }

    private static void setSharedPrefs(String key, boolean value) {
        sEditor.putBoolean(key, value);
        sEditor.commit();
    }

    private static void setSharedPrefs(String key, int value) {
        sEditor.putInt(key, value);
        sEditor.commit();
    }

    private static void removeSharedPrefs(String key) {
        sEditor.remove(key);
        sEditor.commit();
    }

    private static void removeSharedPrefs(String... keys) {
        for (int i=0; keys != null && i<keys.length; i++) {
            sEditor.remove(keys[i]);
        }
        sEditor.commit();
    }

    public static void setCachePolicy(int policy) {
        sCachePolicy = policy;
        setSharedPrefs(PREFS_CACHEPOLICY, sCachePolicy);
    }

    public static float getScreenDensity(Context ctx) {
        if (deviceScreenDensity == 0.0f && ctx != null)
            deviceScreenDensity = ctx.getResources().getDisplayMetrics().density;
        return deviceScreenDensity;
    }

    public static int getScreenWidth(Context ctx){
        if (deviceScreenWidth == -1 && ctx != null){
            WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getRealMetrics(metrics);
            deviceScreenWidth = metrics.widthPixels;
        }
        return deviceScreenWidth;
    }

    public static int getScreenHeight(Context ctx){
        if (deviceScreenHeight == -1 && ctx != null){
            WindowManager wm = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getRealMetrics(metrics);
            deviceScreenHeight = metrics.heightPixels;
        }
        return deviceScreenHeight;
    }


    public static int getCachePolicy() {
        return sCachePolicy;
    }

    public static String getUserAgent() {
        return sUserAgent;
    }

    public static String getClientCountryCode(){
        return sClientCountryCode;
    }

    public static void setClientCountryCode(String country) {
        sClientCountryCode = country;

    }

    public static Locale getLocale() {
        return Locale.US;
    }

    public static void setSubtitleOn(boolean bOnOf){
        sAppSettings.setSubtitleOn(bOnOf);
    }

    public static boolean getSubtitleOn(){
        return sAppSettings.getSubtitleOn();
    }

    public static void launchChromeWithUrl(String urlString){

        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        //Intent i = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(urlString));
        //startActivity(i);



        //intent.setPackage("com.android.chrome");
        try {
            getContext().startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // Chrome browser presumably not installed so allow user to choose instead
            intent.setPackage(null);
            getContext().startActivity(intent);
        }
    }
}
