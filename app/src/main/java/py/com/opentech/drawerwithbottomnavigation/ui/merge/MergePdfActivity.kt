package py.com.opentech.drawerwithbottomnavigation.ui.merge

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import kotlinx.android.synthetic.main.activity_merge_pdf_layout.*
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import py.com.opentech.drawerwithbottomnavigation.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class MergePdfActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge_pdf_layout)
        ButterKnife.bind(this)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Merge PDF"

        addFile.setOnClickListener {
            var intent = Intent(this, MultiFileSelectActivity::class.java)
            startActivity(intent)
        }
    }




    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

}