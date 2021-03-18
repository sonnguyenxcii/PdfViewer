package py.com.opentech.drawerwithbottomnavigation

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.ads.control.Admod
import com.ads.control.funtion.AdCallback
import com.google.android.gms.ads.LoadAdError
import kotlinx.android.synthetic.main.activity_splash_screen.*
import py.com.opentech.drawerwithbottomnavigation.utils.Constants


class SplashScreen : AppCompatActivity() {

    var i: Int = 0
    var count = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SplashTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        
        progressBar.progress = i
        val mCountDownTimer = object : CountDownTimer(2000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                i++
                progressBar.progress = i as Int * 100 / (2000 / 100)
            }

            override fun onFinish() {
                i++
                progressBar.progress = 100
                process()
            }
        }

        mCountDownTimer.start()

       var  timeoutInMilliseconds : Long = 2000
        Admod.getInstance().loadSplashInterstitalAds(this,
            Constants.ADMOB_Interstitial,
            timeoutInMilliseconds,
            object : AdCallback() {
                override fun onAdOpened() {
                    super.onAdOpened()
                }

                override fun onAdClosed() {
                    process()
                }

                override fun onAdFailedToLoad(i: Int) {

                    process()
                }

                override fun onAdFailedToLoad(i: LoadAdError?) {
                    super.onAdFailedToLoad(i)

                }

                override fun onAdImpression() {
                    super.onAdImpression()

                }

            })

    }

    fun process(){

        try {
            count++
            if (count ==1) {
                gotoMain()
            }
        }catch (e:Exception){

        }

    }

    fun gotoMain() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}