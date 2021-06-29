package py.com.opentech.drawerwithbottomnavigation.ui.pdf

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ads.control.Admod
import com.ads.control.funtion.AdCallback
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R

class PdfViewerInAppAdsLoadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preload_ads)
        Admod.getInstance().forceShowInterstitial(
            this,
            PdfApplication.create(this).mInterstitialClickOpenAd,
            object : AdCallback() {
                override fun onAdClosed() {
                    super.onAdClosed()
                    gotoRead()
                }
            })
    }

    fun gotoRead() {
        val intent = Intent(this, PdfViewerActivity::class.java)
        intent.putExtra("url", path)
        startActivity(intent)
        finish()
    }

    private val path: String?
        private get() = intent.getStringExtra("url")
}