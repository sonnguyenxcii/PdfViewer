package py.com.opentech.drawerwithbottomnavigation.ui.library

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.ads.control.Admod
import kotlinx.android.synthetic.main.activity_merge_pdf_layout.*
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.ui.merge.MultiFileSelectActivity
import py.com.opentech.drawerwithbottomnavigation.utils.Constants

class EbookActivity  : AppCompatActivity() {
    protected var application: PdfApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge_pdf_layout)
        ButterKnife.bind(this)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Merge PDF"
        application = PdfApplication.create(this)

        Admod.getInstance().loadNative(this, Constants.ADMOB_Native_Merge_PDF)
        if (application?.mInterstitialMergeAd == null) {
            application?.mInterstitialMergeAd = Admod.getInstance()
                .getInterstitalAds(this, Constants.ADMOB_Interstitial_Merge_PDF)
        }
        addFile.setOnClickListener {

            val params = Bundle()
            params.putString("button_click", "Click+to select the PDF file")
            application?.firebaseAnalytics?.logEvent("Merge_PDF_Layout", params)

            var intent = Intent(this, MultiFileSelectActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}