package py.com.opentech.drawerwithbottomnavigation.ui.pdf

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import androidx.loader.content.CursorLoader
import com.github.barteksc.pdfviewer.PDFView
import io.realm.Realm
import py.com.opentech.drawerwithbottomnavigation.BuildConfig
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.realm.RecentRealmObject
import java.io.File


/**
 * Shows the terms and agreements. Simply calls a webview.
 */
class PdfViewerActivity : AppCompatActivity() {

    var isSwipeHorizontal = false
    var pdfView: PDFView? = null
    var seekBar: SeekBar? = null
    var url: String? = null
    var currentPage = 0
    var viewType = 0 // 0: file, 1: content
    var fileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)
        pdfView = findViewById<PDFView>(R.id.pdfView)
        val progress = findViewById<ProgressBar>(R.id.progress)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val rotate = findViewById<View>(R.id.rotate)
        val share = findViewById<View>(R.id.share)
        val root = findViewById<View>(R.id.root)
        seekBar = findViewById<SeekBar>(R.id.seekBar)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = ""

        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_VIEW == action && type?.endsWith("pdf")!!) {

            // Get the file from the intent object
            val file_uri = intent.data
            println("----------------------file_uri--------" + file_uri)
            viewType = 1

            if (file_uri != null){
                fileUri = file_uri
                viewFileFromStream()
            }else{

            }

        } else {
            url = intent.extras!!.getString("url")
            println("------url------------------------" + url)
            viewType = 0
            url?.let {
                viewFile()
                addToRecent(it)
            }
        }

        rotate.setOnClickListener {
            isSwipeHorizontal = !isSwipeHorizontal
            if (viewType==0){
                viewFile()
            }else{
                viewFileFromStream()
            }
        }
        share.setOnClickListener {
            url?.let { it1 -> shareFile(it1) }
            fileUri?.let {
                shareFileUri(it)
            }
        }

        seekBar!!.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                println("-progress---------------------------" + progress)
                if (fromUser) {
                    pdfView!!.jumpTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

//    val onSeekChangeListener = OnSeekChangeListener(){}

    fun viewFile() {
        val thread = Thread {
            try {
//                    InputStream input = new URL(url).openStream();
                pdfView!!.fromFile(File(url)) //                    pdfView.fromFile(new File("file://"+url))
                    //                    pdfView.fromStream(input)
                    .enableSwipe(true) // allows to block changing pages using swipe
                    .swipeHorizontal(isSwipeHorizontal)
                    .enableDoubletap(true)
                    .defaultPage(currentPage)
                    .fitEachPage(true)
                    .onPageChange { page: Int, pageCount: Int ->
                        currentPage = page
                        println("----onPageChange-----------------------" + page)

                        seekBar?.progress = page
//                        Toast.makeText(
//                            this@PdfViewerActivity,
//                            "Trang " + (page + 1) + "/" + pageCount,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    } // allows to draw something on the current page, usually visible in the middle of the screen
                    // allows to draw something on all pages, separately for every page. Called only for visible pages
                    .onLoad {
                        seekBar!!.max = it - 1
                        println("----onLoadDone-----------------------" + it)
//                        skeletonScreen.hide()
//                        progress!!.visibility = View.GONE
                    } // called after document is loaded and starts to be rendered
                    //                            .nightMode(true)
                    .enableAnnotationRendering(true) // render annotations (such as comments, colors or forms)
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
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun addToRecent(path: String) {
        var model = getRecentByPath(path)
        if (model.isNullOrEmpty()) {
            saveRecent(path)
        } else {
            model[0]?.let { updateRecent(it) }
        }
    }

    fun updateRecent(model: RecentRealmObject) {
        var realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        model.time = System.currentTimeMillis()
        realm.commitTransaction()
    }

    fun saveRecent(path: String) {
        var realm = Realm.getDefaultInstance()
        var id = PdfApplication.recentPrimaryKey.getAndIncrement()

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

    fun shareFile(path: String) {
        val intentShareFile = Intent(Intent.ACTION_SEND)
        val fileWithinMyDir = File(path)

        if (fileWithinMyDir.exists()) {
            intentShareFile.type = "application/pdf"
            val photoURI = let {
                FileProvider.getUriForFile(
                    it, BuildConfig.APPLICATION_ID + ".provider",
                    fileWithinMyDir
                )
            }
            intentShareFile.putExtra(Intent.EXTRA_STREAM, photoURI)
            intentShareFile.putExtra(
                Intent.EXTRA_SUBJECT,
                "Sharing File..."
            )
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
            startActivity(Intent.createChooser(intentShareFile, "Share File"))
        }
    }
    fun shareFileUri(uri: Uri) {
        val intentShareFile = Intent(Intent.ACTION_SEND)
//        val fileWithinMyDir = File(path)

//        if (fileWithinMyDir.exists()) {
            intentShareFile.type = "application/pdf"
//            val photoURI = let {
//                FileProvider.getUriForFile(
//                    it, BuildConfig.APPLICATION_ID + ".provider",
//                    fileWithinMyDir
//                )
//            }
            intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)
            intentShareFile.putExtra(
                Intent.EXTRA_SUBJECT,
                "Sharing File..."
            )
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
            startActivity(Intent.createChooser(intentShareFile, "Share File"))
//        }
    }

    fun viewFileFromStream() {
        val thread = Thread {
            try {
                pdfView!!.fromUri(fileUri)
                    .enableSwipe(true) // allows to block changing pages using swipe
                    .swipeHorizontal(isSwipeHorizontal)
                    .enableDoubletap(true)
                    .defaultPage(currentPage)
                    .fitEachPage(true)
                    .onPageChange { page: Int, pageCount: Int ->
                        currentPage = page
                        println("----onPageChange-----------------------" + page)

                        seekBar?.progress = page

                    } // allows to draw something on the current page, usually visible in the middle of the screen
                    // allows to draw something on all pages, separately for every page. Called only for visible pages
                    .onLoad {
                        seekBar!!.max = it - 1
                        println("----onLoadDone-----------------------" + it)

                    } // called after document is loaded and starts to be rendered
                    //                            .nightMode(true)
                    .enableAnnotationRendering(true) // render annotations (such as comments, colors or forms)
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
    }

}