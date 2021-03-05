package py.com.opentech.drawerwithbottomnavigation.ui.merge

import android.os.Bundle
import android.os.Environment
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import py.com.opentech.drawerwithbottomnavigation.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream


class MergePdfActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_scan_pdf_layout)
        ButterKnife.bind(this)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Máy quét"

    }

    @Throws(IOException::class)
    private fun downloadAndCombinePDFs(): File {
        val ut = PDFMergerUtility()
//        ut.addSource(streamToPdf1)
//        ut.addSource(streamToPdf2)
//        ut.addSource(streamToPdf3)
        val file =
            File(
                Environment.getDownloadCacheDirectory(),
                System.currentTimeMillis().toString() + ".pdf"
            )
        val fileOutputStream = FileOutputStream(file)
        try {
            ut.destinationStream = fileOutputStream
            ut.mergeDocuments(MemoryUsageSetting.setupTempFileOnly())
        } finally {
            fileOutputStream.close()
        }
        return file
    }
}