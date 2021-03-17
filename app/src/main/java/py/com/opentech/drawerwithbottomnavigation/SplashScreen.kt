package py.com.opentech.drawerwithbottomnavigation

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash_screen.*


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
                gotoMain()
            }
        }

        mCountDownTimer.start()

    }

    fun gotoMain() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}