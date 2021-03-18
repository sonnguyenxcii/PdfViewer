package py.com.opentech.drawerwithbottomnavigation.ui.pdf

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.github.barteksc.pdfviewer.PDFView
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import com.hosseiniseyro.apprating.AppRatingDialog
import com.hosseiniseyro.apprating.listener.RatingDialogListener
import io.realm.Realm
import py.com.opentech.drawerwithbottomnavigation.BuildConfig
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.realm.RecentRealmObject
import java.io.File
import java.util.*


/**
 * Shows the terms and agreements. Simply calls a webview.
 */
class PdfViewerActivity : AppCompatActivity(), RatingDialogListener {

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
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        val rotate = findViewById<View>(R.id.rotate)
        val share = findViewById<View>(R.id.share)
        seekBar = findViewById<SeekBar>(R.id.seekBar)

        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = ""

        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_VIEW == action && type?.endsWith("pdf")!!) {
            val file_uri = intent.data
            viewType = 1

            if (file_uri != null) {
                fileUri = file_uri
                viewFileFromStream()
            } else {

            }

        } else {
            url = intent.extras!!.getString("url")
            viewType = 0
            url?.let {
                viewFile()
                addToRecent(it)
            }
        }

        rotate.setOnClickListener {
            isSwipeHorizontal = !isSwipeHorizontal
            if (viewType == 0) {
                viewFile()
            } else {
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


    fun viewFile() {
        val thread = Thread {
            try {
                pdfView!!.fromFile(File(url))
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
//        onBackPressed()
        showRate()
        return true
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        showRate()
    }

    fun showRate() {
        AppRatingDialog.Builder()
            .setPositiveButtonText("Submit")
            .setNegativeButtonText("Cancel")
            .setNeutralButtonText("Later")
            .setNoteDescriptions(
                Arrays.asList(
                    "Very Bad",
                    "Not good",
                    "Quite ok",
                    "Very Good",
                    "Excellent !!!"
                )
            )
            .setDefaultRating(5)
            .setThreshold(3)
            .setTitle("Did you like the app?")
            .setDescription("Let us know what you think")
            .setCommentInputEnabled(true)
            .setDefaultComment("This app is pretty cool !")
            .setStarColor(R.color.navBgColor)
            .setNoteDescriptionTextColor(R.color.colorPrimaryDark)
            .setTitleTextColor(R.color.black)
            .setDescriptionTextColor(R.color.descriptionTextColor)
            .setHint("Please write your comment here ...")
            .setHintTextColor(R.color.white)
            .setCommentTextColor(R.color.white)
            .setCommentBackgroundColor(R.color.blue50)
            .setDialogBackgroundColor(R.color.white)
            .setCancelable(false)
            .setCanceledOnTouchOutside(false)
            .create(this)
            .show()
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
        intentShareFile.type = "application/pdf"
        intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)
        intentShareFile.putExtra(
            Intent.EXTRA_SUBJECT,
            "Sharing File..."
        )
        intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
        startActivity(Intent.createChooser(intentShareFile, "Share File"))
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

                        seekBar?.progress = page

                    } // allows to draw something on the current page, usually visible in the middle of the screen
                    // allows to draw something on all pages, separately for every page. Called only for visible pages
                    .onLoad {
                        seekBar!!.max = it - 1

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

    override fun onNegativeButtonClicked() {
        finish()
    }

    override fun onNeutralButtonClicked() {
        finish()

    }

    override fun onPositiveButtonClickedWithComment(rate: Int, comment: String) {
        if (rate>=4){
            askRatings()

        }else{
            finish()
        }
    }

    override fun onPositiveButtonClickedWithoutComment(rate: Int) {
        if (rate>=4){
            askRatings()

        }else{
            finish()
        }
    }

    fun askRatings() {
        val manager = ReviewManagerFactory.create(this)
        val request: Task<ReviewInfo> = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                val reviewInfo: ReviewInfo = task.getResult()
                val flow: Task<Void> = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { task2 ->
                    finish()
                }
            } else {
                finish()
                // There was some problem, continue regardless of the result.
            }
        }
    }

}