package py.com.opentech.drawerwithbottomnavigation

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.ads.control.Admod
import com.ads.control.funtion.AdCallback
import com.google.android.gms.ads.*
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_splash_screen.*
import kotlinx.android.synthetic.main.include_preload_ads.*
import py.com.opentech.drawerwithbottomnavigation.utils.Constants
import py.com.opentech.drawerwithbottomnavigation.utils.InternetConnection
import py.com.opentech.drawerwithbottomnavigation.utils.admob.InterstitialUtils


class SplashScreen : AppCompatActivity() {

    var i: Int = 0
    var time = 15000
    var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        countOpenApp()
        InterstitialUtils.initInterstitialStartup(this)
//        Thread{
        if (InternetConnection.checkConnection(this)) {
            prepareAds()
        } else {
            time = 3000
        }

//        progressBar.progress = i
        val mCountDownTimer = object : CountDownTimer(time.toLong(), 100) {
            override fun onTick(millisUntilFinished: Long) {
                i++
//                progressBar.progress = i * 100 / (time / 100)
            }

            override fun onFinish() {

                i++
//                progressBar.progress = 100
                mAnimationDone = true
                if (!mPrepareAdsDone) {
                    gotoMain()
                }
            }
        }

        mCountDownTimer.start()
//        }.start()


    }


    fun gotoMain() {
        println("--gotoMain-------------count--" + count)

        if (count == 0) {
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            count++
            Handler(Looper.getMainLooper()).postDelayed({
                finish()
            }, 1000)

        }
        println("--gotoMain-------------count-2-" + count)

    }

    private var mPrepareAdsDone = false
    private var mAnimationDone = false

    private fun prepareAds() {

        Admod.getInstance().loadSplashInterstitalAds(this,
            Constants.ADMOB_Interstitial_Splash,
            time.toLong(),
            object : AdCallback() {
                override fun onAdImpression() {
                    super.onAdImpression()
                    println("--onAdImpression---------------" + mPrepareAdsDone)

                }

                override fun onAdLoaded() {
                    super.onAdLoaded()
                    mPrepareAdsDone = true

                    println("--onAdLoaded---------------" + mPrepareAdsDone)
                }

                override fun onAdClosed() {
                    mPrepareAdsDone = true

//                    if (mAnimationDone) {
                    gotoMain()
//                    }

                }

                override fun onAdFailedToLoad(i: Int) {
                    mPrepareAdsDone = true

//                    if (mAnimationDone) {
                    gotoMain()
//                    }
                }
            })

    }

    override fun onPause() {
        super.onPause()
        mPrepareAdsDone = true

    }

    fun countOpenApp() {
        val prefs = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE)
        var count = prefs.getInt("openAppCount", 0)
        count++
        var editor = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE).edit()
        editor.putInt("openAppCount", count)
        editor.apply()
    }

}