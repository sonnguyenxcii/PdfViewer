package py.com.opentech.drawerwithbottomnavigation.ui.pdf

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ads.control.Admod
import com.ads.control.funtion.AdCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.utils.Constants

class PdfViewerAdsLoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preload_ads)
        loadAds()
    }

    fun loadAds() {
        val prefs = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE)
        val time = prefs.getLong("openFromOtherAppTimeOut", 5000)
        Admod.getInstance().loadSplashInterstitalAds(this,
            Constants.ADMOB_Iterstitial_Open_From_Other_App,
            time,
            object : AdCallback() {

                override fun onAdClosed() {
//                    if (mAnimationDone) {
                    gotoRead()
//                    }

                }

                override fun onAdFailedToLoad(i: Int) {

//                    if (mAnimationDone) {
                    gotoRead()
//                    }
                }
            })
//        MobileAds.initialize(this, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {
//            }
//        });
//        AdRequest adRequest = new AdRequest.Builder().build();
//
//        InterstitialAd.load(this, Constants.ADMOB_Iterstitial_Open_From_Other_App, adRequest, new InterstitialAdLoadCallback() {
//            @Override
//            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                // The mInterstitialAd reference will be null until
//                // an ad is loaded.
//                mInterstitialAd = interstitialAd;
////                Log.i(TAG, "onAdLoaded");
//                if (mInterstitialAd != null) {
//                    mInterstitialAd.show(PdfViewerAdsLoadingActivity.this);
//
//                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
//                        @Override
//                        public void onAdDismissedFullScreenContent() {
//                            // Called when fullscreen content is dismissed.
//                            Log.d("TAG", "The ad was dismissed.");
//                            gotoRead();
//                        }
//
//                        @Override
//                        public void onAdFailedToShowFullScreenContent(AdError adError) {
//                            // Called when fullscreen content failed to show.
//                            Log.d("TAG", "The ad failed to show.");
//                            gotoRead();
//                        }
//
//                        @Override
//                        public void onAdShowedFullScreenContent() {
//                            // Called when fullscreen content is shown.
//                            // Make sure to set your reference to null so you don't
//                            // show it a second time.
//                            mInterstitialAd = null;
//                            Log.d("TAG", "The ad was shown.");
//                        }
//                    });
//                } else {
//                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
//                }
//
//
//            }
//
//            @Override
//            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                // Handle the error
//                mInterstitialAd = null;
//                gotoRead();
//            }
//        });
    }

    fun gotoRead() {
        val intent = Intent(this, PdfViewerActivity::class.java)
        intent.data = getIntent().data
        startActivity(intent)
        finish()
    }
}