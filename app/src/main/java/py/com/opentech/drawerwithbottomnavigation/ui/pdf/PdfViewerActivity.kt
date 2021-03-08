package py.com.opentech.drawerwithbottomnavigation.ui.pdf

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import butterknife.BindView
import butterknife.ButterKnife
import com.github.barteksc.pdfviewer.PDFView
import io.realm.Realm
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.realm.BookmarkRealmObject
import py.com.opentech.drawerwithbottomnavigation.model.realm.RecentRealmObject
import java.io.File

/**
 * Shows the terms and agreements. Simply calls a webview.
 */
class PdfViewerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)
       val pdfView = findViewById<PDFView>(R.id.pdfView)
       val progress = findViewById<ProgressBar>(R.id.progress)
//        ButterKnife.bind(this)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        val url = intent.extras!!.getString("url")

                System.out.println("----url-------"+url);
        val thread = Thread {
            try {
//                    InputStream input = new URL(url).openStream();
                pdfView.fromFile(File(url)) //                    pdfView.fromFile(new File("file://"+url))
                    //                    pdfView.fromStream(input)
                    .enableSwipe(true) // allows to block changing pages using swipe
                    .swipeHorizontal(false)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .fitEachPage(true)
                    .onPageChange { page: Int, pageCount: Int ->
                        Toast.makeText(
                            this@PdfViewerActivity,
                            "Trang " + (page + 1) + "/" + pageCount,
                            Toast.LENGTH_SHORT
                        ).show()
                    } // allows to draw something on the current page, usually visible in the middle of the screen
                    // allows to draw something on all pages, separately for every page. Called only for visible pages
                    .onLoad {
                        progress!!.visibility = View.GONE
                    } // called after document is loaded and starts to be rendered
                    //                            .nightMode(true)
                    .enableAnnotationRendering(false) // render annotations (such as comments, colors or forms)
                    .password(null)
                    .scrollHandle(null)
                    .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                    .spacing(10)
                    .load()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
        url?.let { addToRecent(it) }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun addToRecent(path: String) {
        var model = getRecentByPath(path)
        if (model.isNullOrEmpty()) {
            saveRecent(path)
        }else{
            model[0]?.let { updateRecent(it) }
        }
    }

    fun updateRecent(model : RecentRealmObject){
        var realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        model.time = System.currentTimeMillis()
        realm.commitTransaction()
    }

    fun saveRecent(path: String) {
        var realm = Realm.getDefaultInstance()
        var id = PdfApplication.recentPrimaryKey.getAndIncrement();

        realm.executeTransactionAsync { realm ->
            val model: RecentRealmObject =
                realm?.createObject(RecentRealmObject::class.java, id)!!
            model.path = path
            model.time = System.currentTimeMillis()
        }
    }
    fun getRecentByPath(path: String): List<RecentRealmObject?>? {
        var realm = Realm.getDefaultInstance()

        return realm.where(RecentRealmObject::class.java).equalTo("path", path).findAll()
    }


}