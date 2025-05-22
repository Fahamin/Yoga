package com.livetv.configurator.nexus.kodiapps.core;


import static android.content.Context.CLIPBOARD_SERVICE;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.webkit.MimeTypeMap;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.livetv.configurator.nexus.kodiapps.R;

import java.io.File;


public class Fun {

    public static String appurl = "https://play.google.com/store/apps/details?id=com.livetv.configurator.nexus.kodiapps";
    private static int count = 0;
    public static Activity activity;
    private static com.facebook.ads.InterstitialAd interstitialAd;
    private static InterstitialAd mInterstitialAd;
    private static int divider = 3;
    public static int nc = 15;

    private static int fc = 2;

    public static DatabaseReference referenceadmob = FirebaseDatabase.getInstance().getReference("adid");
    private FrameLayout adContainerView;
    static AdView adView;
    private static com.facebook.ads.AdView adViewFb;

    public static String sc = "0";

    public Fun(Activity activity) {
        this.activity = activity;
        getSc();
        getDiv();
        getfc();
        getNc();

    }

    private void getSc() {
        referenceadmob.child("sc").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sc = String.valueOf(dataSnapshot.getValue());
                Log.e("fahamin sc ", sc.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public static void showBannerAdmob(Activity activity, FrameLayout adContainerView) {
        // Step 1 - Create an AdView and set the ad unit ID on it.
        adView = new AdView(activity);
        adView.setAdUnitId(activity.getString(R.string.admob_banner_id));
        adContainerView.addView(adView);

        if (!sc.equals("0")) {
            loadBanner(activity);
        }

        adView.setAdListener(new com.google.android.gms.ads.AdListener() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                showBannerFb(activity, adContainerView);
            }
        });
    }

    public static void showBanner(Activity activity, FrameLayout adContainerView) {

        if (!sc.equals("0")) {
            showBannerAdmob(activity, adContainerView);
        }
    }

    public static void showBannerFb(Activity activity, FrameLayout adContainerView) {
        adViewFb = new com.facebook.ads.AdView(activity, activity.getString(R.string.fb_banner_id), com.facebook.ads.AdSize.BANNER_HEIGHT_50);
        adContainerView.addView(adViewFb);

        AdListener adListener = new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                showBannerAdmob(activity, adContainerView);

            }

            @Override
            public void onAdLoaded(Ad ad) {

            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        };

        adViewFb.loadAd(adViewFb.buildLoadAdConfig().withAdListener(adListener).build());

    }


    private static void loadBanner(Activity activity) {
        // Create an ad request. Check your logcat output for the hashed device ID
        // to get test ads on a physical device, e.g.,
        // "Use AdRequest.Builder.addTestDevice("ABCDE0123") to get test ads on this
        // device."
        AdRequest adRequest =
                new AdRequest.Builder()
                        .build();

        AdSize adSize = getAdSize(activity);
        // Step 4 - Set the adaptive ad size on the ad view.
        adView.setAdSize(adSize);


        // Step 5 - Start loading the ad in the background.
        adView.loadAd(adRequest);
    }

    private static AdSize getAdSize(Activity activity) {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics = new DisplayMetrics();
        display.getMetrics(outMetrics);

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth);
    }


    private void getNc() {
        referenceadmob.child("nc").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String ss = (String) dataSnapshot.getValue();
                nc = Integer.valueOf(ss);

                if (sc.equals("0")) {
                    nc = 1000;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    public static void getDiv() {
        referenceadmob.child("div").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String ss = (String) dataSnapshot.getValue();
                divider = Integer.valueOf(ss);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public static void getfc() {
        referenceadmob.child("fc").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String ss = (String) dataSnapshot.getValue();
                fc = Integer.valueOf(ss);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private static BroadcastReceiver attachmentDownloadCompleteReceive;

    public static boolean checkInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(activity.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();

        if (info != null && info.isConnected()) {
            return true;
        } else {
            return false;

        }
    }


    private static int ac = 0;


    public static void addShowAdmob() {


        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(activity, activity.getString(R.string.admob_insta_id), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {

                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        if (mInterstitialAd != null) {
                            mInterstitialAd.show(activity);
                        } else addShowFb();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {

                        Log.i("MainActivity", loadAdError.getMessage());
                        mInterstitialAd = null;
                    }

                });


    }

    public static void addShow() {
        count++;

        if (!sc.equals("0")) {

            if (count % divider == 0) {
                addShowAdmob();
            }

            if (count % fc == 0) {
                addShowFb();
            }
        }


    }


    public static void addShowFb() {


        if (!sc.equals("0")) {


            interstitialAd = new com.facebook.ads.InterstitialAd(activity, activity.getString(R.string.fb_insta_id));
            // Create listeners for the Interstitial Ad
            InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
                @Override
                public void onInterstitialDisplayed(Ad ad) {
                    // Interstitial ad displayed callback

                }

                @Override
                public void onInterstitialDismissed(Ad ad) {
                    // Interstitial dismissed callback

                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    // Ad error callback

                }

                @Override
                public void onAdLoaded(Ad ad) {
                    // Interstitial ad is loaded and ready to be displayed

                    // Show the ad
                    interstitialAd.show();
                }

                @Override
                public void onAdClicked(Ad ad) {
                    // Ad clicked callback

                }

                @Override
                public void onLoggingImpression(Ad ad) {
                    // Ad impression logged callback

                }
            };

            // For auto play video ads, it's recommended to load the ad
            // at least 30 seconds before it is shown
            interstitialAd.loadAd(
                    interstitialAd.buildLoadAdConfig()
                            .withAdListener(interstitialAdListener)
                            .build());

        }

    }

}