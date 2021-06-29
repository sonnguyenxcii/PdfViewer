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
import py.com.opentech.drawerwithbottomnavigation.utils.InternetConnection

class OfficeReaderAdsLoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preload_ads)
        loadAds()
    }

    fun loadAds() {

        if (!InternetConnection.checkConnection(this)) {
            gotoRead()
            return
        }

        Admod.getInstance().loadSplashInterstitalAds(this,
            Constants.ADMOB_Interstitial_Splash,
            5000,
            object : AdCallback() {
                override fun onAdImpression() {
                    super.onAdImpression()

                }

                override fun onAdLoaded() {
                    super.onAdLoaded()

                }

                override fun onAdClosed() {
                    gotoRead()
                }

                override fun onAdFailedToLoad(i: Int) {
                    gotoRead()
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