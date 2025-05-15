package com.livetv.configurator.nexus.kodiapps.core

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import com.facebook.ads.*
import com.facebook.ads.AdError
import com.github.mikephil.charting.utils.Utils
import com.livetv.configurator.nexus.kodiapps.R

import com.google.android.gms.ads.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.livetv.configurator.nexus.kodiapps.core.interfaces.AdsCallback
import com.livetv.configurator.nexus.kodiapps.core.interfaces.CallbackListener



internal object AdUtils {


    fun loadGoogleBannerAd(context: Context, llAdview: RelativeLayout, type: String) {
        try {


            var adViewBottom: AdView = AdView(context)
            if (type == Constant.BANNER_TYPE) {
                adViewBottom.setAdSize(AdSize.FULL_BANNER)
            } else if (type == Constant.REC_BANNER_TYPE) {
                adViewBottom.setAdSize(AdSize.MEDIUM_RECTANGLE)
            }
            adViewBottom.adUnitId = Constant.GOOGLE_BANNER_ID
            llAdview.addView(adViewBottom)
            val adRequest = AdRequest.Builder().build()
            adViewBottom.loadAd(adRequest)
            adViewBottom.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    adViewBottom.visibility = View.VISIBLE
                    llAdview.visibility = View.VISIBLE
                }

                override fun onAdFailedToLoad(p0: LoadAdError) {
                    Log.e("TAG", "onAdFailedToLoad:Faild :::  $p0")
                    super.onAdFailedToLoad(p0!!)
                }
            }


        } catch (e: Exception) {
        }
    }



    var mInterstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd? = null
    fun loadGoogleFullAd(context: Context, adsCallback: AdsCallback) {

            showAdsDialog(context)

            val adRequest = AdRequest.Builder().build()

            com.google.android.gms.ads.interstitial.InterstitialAd.load(
                context, Constant.GOOGLE_INTERSTITIAL_ID,
                adRequest, object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: com.google.android.gms.ads.interstitial.InterstitialAd) {
                        super.onAdLoaded(interstitialAd)
                        mInterstitialAd = interstitialAd
                        if (adsDialog != null && adsDialog!!.isShowing) {
                            adsDialog!!.dismiss()
                        }
                        mInterstitialAd?.fullScreenContentCallback = object :
                            FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                super.onAdDismissedFullScreenContent()
                                adsCallback.startNextScreenAfterAd()
                            }

                            override fun onAdFailedToShowFullScreenContent(p0: com.google.android.gms.ads.AdError) {
                                super.onAdFailedToShowFullScreenContent(p0)
                                Log.d("TAG", "Ad failed to show.")
                                adsCallback.startNextScreenAfterAd()
                            }

                            override fun onAdShowedFullScreenContent() {
                                super.onAdShowedFullScreenContent()
                                Log.d("TAG", "Ad showed fullscreen content.")
                            }
                        }
                        mInterstitialAd?.show(context as Activity)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        Log.d("TAG", "Failed to load interstitial ad: ${adError.message}")
                        if (adsDialog != null && adsDialog!!.isShowing) {
                            adsDialog!!.dismiss()
                        }
                        adsCallback.startNextScreenAfterAd()
                    }
                })
        }



    fun loadFacebookFullAd(context: Context, adsCallback: AdsCallback) {

            showAdsDialog(context)
            var interstitialAd = InterstitialAd(context, Constant.FB_INTERSTITIAL_ID)
            val interstitialAdListener: InterstitialAdListener = object : InterstitialAdListener {
                override fun onInterstitialDisplayed(ad: Ad?) {
                    Log.e("TAG", "Interstitial ad displayed.")

                }

                override fun onInterstitialDismissed(ad: Ad?) {
                    Log.e("TAG", "Interstitial ad dismissed.")
                    adsCallback.startNextScreenAfterAd()
                }

                override fun onError(ad: Ad?, adError: AdError) {
                    if (adsDialog != null && adsDialog!!.isShowing) {
                        adsDialog!!.dismiss()
                    }
                    adsCallback.startNextScreenAfterAd()
                }

                override fun onAdLoaded(ad: Ad?) {
                    Log.e("TAG", "Interstitial ad is loaded and ready to be displayed!")
                    // Show the ad
                    if (adsDialog != null && adsDialog!!.isShowing) {
                        adsDialog!!.dismiss()
                    }
                    interstitialAd.show()
                }

                override fun onAdClicked(ad: Ad?) {
                    Log.d("TAG", "Interstitial ad clicked!")
                }

                override fun onLoggingImpression(ad: Ad?) {
                    Log.d("TAG", "Interstitial ad impression logged!")
                }
            }

            interstitialAd.loadAd(
                interstitialAd.buildLoadAdConfig()
                    .withAdListener(interstitialAdListener)
                    .build()
            )
        }



    fun loadFacebookBannerAd(context: Context, banner_container: LinearLayout) {

            Log.e("TAG", "loadFbAdFacebook::::::::::: ")
            var adView: com.facebook.ads.AdView? = null
            adView =
                com.facebook.ads.AdView(
                    context,
                    Constant.FB_BANNER_ID,
                    com.facebook.ads.AdSize.BANNER_HEIGHT_50
                )


            banner_container.addView(adView)

            val adListener: com.facebook.ads.AdListener = object : com.facebook.ads.AdListener {
                override fun onError(ad: Ad?, adError: AdError) {
                    // Ad error callback
                    Log.e("TAG", "onError:Fb:::: $adError")
                    banner_container.visibility = View.GONE
                }

                override fun onAdLoaded(ad: Ad?) {
                    // Ad loaded callback
                    Log.e("TAG", "onAdLoaded:::::: ")
                    banner_container.visibility = View.VISIBLE
                }

                override fun onAdClicked(ad: Ad?) {
                    // Ad clicked callback
                }

                override fun onLoggingImpression(ad: Ad?) {
                    // Ad impression logged callback
                }


            }

            adView!!.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build())
        }


    fun loadFacebookMediumRectangleAd(context: Context, banner_container: LinearLayout) {

            Log.e("TAG", "loadFbAdFacebook::::::::::: ")
            var adView: com.facebook.ads.AdView? = null

            adView =
                com.facebook.ads.AdView(
                    context,
                    Constant.FB_BANNER_MEDIUM_RECTANGLE_ID,
                    com.facebook.ads.AdSize.RECTANGLE_HEIGHT_250
                )


            banner_container.addView(adView)

            val adListener: com.facebook.ads.AdListener = object : com.facebook.ads.AdListener {
                override fun onError(ad: Ad?, adError: AdError) {
                    // Ad error callback
                    Log.e("TAG", "onError:Fb:::: $adError")
                    banner_container.visibility = View.GONE
                }

                override fun onAdLoaded(ad: Ad?) {
                    // Ad loaded callback
                    Log.e("TAG", "onAdLoaded:::::: ")
                    banner_container.visibility = View.VISIBLE
                }

                override fun onAdClicked(ad: Ad?) {
                    // Ad clicked callback
                }

                override fun onLoggingImpression(ad: Ad?) {
                    // Ad impression logged callback
                }


            }

            adView!!.loadAd(adView.buildLoadAdConfig().withAdListener(adListener).build())

    }

    private var fbRewardedVideoAd: RewardedVideoAd? = null

    fun loadFacebookVideoAd(context: Context, adsCallback: CallbackListener) {

//        fbRewardedVideoAd = RewardedVideoAd(context, Constant.FB_REWARDED_VIDEO)
        fbRewardedVideoAd = RewardedVideoAd(context, Constant.FB_REWARDED_VIDEO_ID)

        val rewardedVideoAdListener: RewardedVideoAdListener = object : RewardedVideoAdListener {
            override fun onRewardedVideoClosed() {
                Log.e("TAG", "onRewardedVideoClosed::::: ")
                adsCallback.onCancel()
            }

            override fun onAdClicked(p0: Ad?) {
                Log.e("TAG", "onAdClicked:::: " + p0.toString())
            }

            override fun onRewardedVideoCompleted() {
                Log.e("TAG", "onRewardedVideoCompleted:::::: ")
                adsCallback.onSuccess()
            }

            override fun onError(p0: Ad?, p1: AdError?) {
                Log.e("TAG", "onError:::::: " + p1.toString())

                adsCallback.onSuccess()
            }

            override fun onAdLoaded(p0: Ad?) {
                Log.e("TAG", "onAdLoaded:::::: " + p0!!.isAdInvalidated + "  " + p0!!.placementId)
            }

            override fun onLoggingImpression(p0: Ad?) {
                Log.e("TAG", "onLoggingImpression::::::: ")
            }

        }

        fbRewardedVideoAd!!.loadAd(
            fbRewardedVideoAd!!.buildLoadAdConfig()
                .withAdListener(rewardedVideoAdListener)
                .build()
        )

        if (fbRewardedVideoAd!!.isAdLoaded) {
            // showing Video Ad
            fbRewardedVideoAd!!.show()
        } else {
            // Loading Video Ad If it  is Not Loaded
            fbRewardedVideoAd!!.loadAd()
        }

        // RewardedVideoAd AdListener

        // RewardedVideoAd AdListener


        // loading Ad

        // loading Ad
//        fbRewardedVideoAd!!.loadAd()
    }

    internal var adsDialog: Dialog? = null
    private fun showAdsDialog(context: Context) {
        adsDialog = Dialog(context)
        adsDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        adsDialog!!.setContentView(R.layout.dialog_full_screen_ad)
        adsDialog!!.setCancelable(false)
        adsDialog!!.show()
    }

}