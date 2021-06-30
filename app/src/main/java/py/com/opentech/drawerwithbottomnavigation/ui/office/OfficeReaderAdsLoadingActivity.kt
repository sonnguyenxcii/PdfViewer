package py.com.opentech.drawerwithbottomnavigation.ui.office

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ads.control.Admod
import com.ads.control.AppPurchase
import com.ads.control.funtion.AdCallback
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.utils.Constants
import py.com.opentech.drawerwithbottomnavigation.utils.InternetConnection
import java.util.*

class OfficeReaderAdsLoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preload_ads)

//        List<String> listINAPId = new ArrayList<>();
//        listINAPId.add(PRODUCT_ID);
//        List<String> listSubsId = new ArrayList<>();

//        AppPurchase.getInstance().initBilling(this);
        val listINAPId: MutableList<String> = ArrayList()
        listINAPId.add(PdfApplication.REMOVE_ADS)
        val listSubsId: List<String> = ArrayList()

        AppPurchase.getInstance().initBilling(PdfApplication.create(this), listINAPId, listSubsId)

        loadAds()
    }

    fun loadAds() {

        if (!InternetConnection.checkConnection(this)) {
            gotoRead()
            return
        }

        Admod.getInstance().loadSplashInterstitalAds(this,
            Constants.ADMOB_Iterstitial_Open_From_Other_App,
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