package py.com.opentech.drawerwithbottomnavigation

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import com.ads.control.Admod
import com.ads.control.funtion.AdCallback
import kotlinx.android.synthetic.main.activity_splash_screen.*
import py.com.opentech.drawerwithbottomnavigation.utils.Constants


class SplashScreen : AppCompatActivity() {

    var i: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.SplashTheme)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        
        progressBar.progress = i
        val mCountDownTimer = object : CountDownTimer(3000, 100) {
            override fun onTick(millisUntilFinished: Long) {
                i++
                progressBar.progress = i as Int * 100 / (3000 / 100)
            }

            override fun onFinish() {
                i++
                progressBar.progress = 100
//                gotoMain()
            }
        }

        mCountDownTimer.start()

       var  timeoutInMilliseconds : Long = 3000
        Admod.getInstance().loadSplashInterstitalAds(this,
            Constants.ADMOB_Interstitial,
            timeoutInMilliseconds,
            object : AdCallback() {
                override fun onAdClosed() {
                    gotoMain()
                }

                override fun onAdFailedToLoad(i: Int) {
                    gotoMain()
                }
            })

//        if (timeoutInMilliseconds<=0){
//            gotoMain()
//        }
    }

    fun gotoMain() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}