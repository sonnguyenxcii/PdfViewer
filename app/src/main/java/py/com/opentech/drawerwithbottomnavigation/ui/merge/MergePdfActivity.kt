package py.com.opentech.drawerwithbottomnavigation.ui.merge

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import kotlinx.android.synthetic.main.activity_merge_pdf_layout.*
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R


class MergePdfActivity : AppCompatActivity() {
    protected var application: PdfApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge_pdf_layout)
        ButterKnife.bind(this)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Merge PDF"
        application = PdfApplication.create(this)

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