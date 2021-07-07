package py.com.opentech.drawerwithbottomnavigation.ui.office

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ads.control.Admod
import com.ads.control.funtion.AdCallback
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.utils.Constants

class OfficeReaderAdsLoadingActivity : AppCompatActivity() {
    private var mInterstitialAd: InterstitialAd? = null
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
    }

    fun gotoRead() {
        val intent = Intent(this, OfficeReaderActivity::class.java)
        intent.data = getIntent().data
        startActivity(intent)
        finish()
    }
}