package py.com.opentech.drawerwithbottomnavigation

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.android.synthetic.main.include_preload_ads.*
import py.com.opentech.drawerwithbottomnavigation.utils.Constants


class SplashScreen : AppCompatActivity() {

    var i: Int = 0

    private var mInterstitialAd: InterstitialAd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SplashTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        prepareAds()

        progressBar.progress = i
        val mCountDownTimer = object : CountDownTimer(3000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                i++
                progressBar.progress = i as Int * 100 / (3000 / 100)
            }

            override fun onFinish() {
                i++
                progressBar.progress = 100
                mAnimationDone = true
                if (!mPrepareAdsDone) {
                    mInterstitialAd!!.adListener = AdListener()
                    gotoMain()
                }
            }
        }

        mCountDownTimer.start()

    }

    fun gotoMain() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private var mPrepareAdsDone = false
    private var mAnimationDone = false

    private fun prepareAds() {
        mPrepareAdsDone = false

        MobileAds.initialize(
            this
        ) { initializationStatus: InitializationStatus? -> }

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd!!.adUnitId = Constants.ADMOB_Interstitial
        mInterstitialAd!!.adListener = mDefaultListener
        val adRequest = AdRequest.Builder().build()
        mInterstitialAd!!.loadAd(adRequest)
    }

    private val mDefaultListener: AdListener = object : AdListener() {
        override fun onAdLoaded() {
            mPrepareAdsDone = true
            super.onAdLoaded()
            checkDoneLoading()

        }

        override fun onAdClosed() {
            gotoMain()
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            mPrepareAdsDone = true
            super.onAdFailedToLoad(loadAdError)
            checkDoneLoading()

        }
    }

    private fun checkDoneLoading() {
        if (mPrepareAdsDone) {
            if (mInterstitialAd != null && mInterstitialAd!!.isLoaded) {
                preloadAdsLayout.visibility = View.VISIBLE

                val mCountDownTimer = object : CountDownTimer(1000, 100) {
                    override fun onTick(millisUntilFinished: Long) {

                    }

                    override fun onFinish() {
                        mInterstitialAd!!.show()
                    }
                }

                mCountDownTimer.start()
            } else {
                if (mAnimationDone){
                    gotoMain()
                }

            }
        }
    }
}