package py.com.opentech.drawerwithbottomnavigation.ui.pdf

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.InputType
import android.text.TextUtils
import android.view.*
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnTapListener
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import com.hosseiniseyro.apprating.AppRatingDialog
import com.hosseiniseyro.apprating.listener.RatingDialogListener
import com.shockwave.pdfium.PdfPasswordException
import io.realm.Realm
import kotlinx.android.synthetic.main.include_preload_ads.*
import py.com.opentech.drawerwithbottomnavigation.BuildConfig
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.realm.RecentRealmObject
import py.com.opentech.drawerwithbottomnavigation.utils.Constants
import py.com.opentech.drawerwithbottomnavigation.utils.Constants.MY_PREFS_NAME
import py.com.opentech.drawerwithbottomnavigation.utils.OnSingleClickListener
import java.io.File
import java.util.*


class PdfViewerActivity : AppCompatActivity(), RatingDialogListener {

    var isSwipeHorizontal = false
    lateinit var pdfView: PDFView
    lateinit var seekBar: AppCompatSeekBar
    var url: String? = null
    var currentPage = 0
    var viewType = 0 // 0: file, 1: content
    var fileUri: Uri? = null
    protected var application: PdfApplication? = null
    var password: String = ""
    lateinit var toolbar: Toolbar
    lateinit var rootView: View
    lateinit var bottom: View
    lateinit var more: View

    var isFullscreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)
        pdfView = findViewById(R.id.pdfView)
        toolbar = findViewById(R.id.toolbar)
        val rotate = findViewById<View>(R.id.rotate)
        val share = findViewById<View>(R.id.share)
        seekBar = findViewById(R.id.seekBar)
        rootView = findViewById(R.id.rootView)
        bottom = findViewById(R.id.bottom)
        more = findViewById(R.id.more)

        try {
            setSupportActionBar(toolbar)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
            supportActionBar!!.title = ""

            val action = intent.action
            val type = intent.type
            application = PdfApplication.create(this)

            if (Intent.ACTION_VIEW == action && type?.endsWith("pdf")!!) {
                val file_uri = intent.data
                viewType = 1
                prepareAds()

                if (file_uri != null) {
                    fileUri = file_uri
                    viewFileFromStream()

                }

            } else {
                url = intent.extras!!.getString("url")
                viewType = 0
                url?.let {
                    addToRecent(it)
                    var temp = getRecentByPath(it)
                    if (!temp.isNullOrEmpty()) {
                        if (temp[0]?.page != null) {
                            try {
                                currentPage = temp[0]?.page!!
                            } catch (e: Exception) {

                            }
                        }
                    }
                    viewFile()
                }
            }
        } catch (e: Exception) {

        }


        rotate.setOnClickListener {
            isSwipeHorizontal = !isSwipeHorizontal
            if (viewType == 0) {
                viewFile()
            } else {
                viewFileFromStream()
            }
        }


        share.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                url?.let { it1 -> shareFile(it1) }
                fileUri?.let {
                    shareFileUri(it)
                }
            }
        })

        more.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                v?.let { onMoreClick(it) }
            }
        })

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    pdfView.jumpTo(progress)
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
                pdfView.fromFile(File(url))
                    .enableSwipe(true) // allows to block changing pages using swipe
                    .swipeHorizontal(isSwipeHorizontal)
                    .enableDoubletap(true)
                    .defaultPage(currentPage)
                    .fitEachPage(true)
                    .onPageChange { page: Int, pageCount: Int ->
                        currentPage = page
                        println("----onPageChange-----------------------" + page)

                        seekBar.progress = page

                    } // allows to draw something on the current page, usually visible in the middle of the screen
                    // allows to draw something on all pages, separately for every page. Called only for visible pages
                    .onLoad {
                        seekBar.max = it - 1

                    }
                    .enableAnnotationRendering(true) // render annotations (such as comments, colors or forms)
                    .password(password)
                    .scrollHandle(null)
                    .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                    .spacing(10)
                    .onError {
                        if (it is PdfPasswordException) {
                            showInputPassword()
                        }
                    }
                    .onTap(object : OnTapListener {
                        override fun onTap(e: MotionEvent?): Boolean {
                            isFullscreen = !isFullscreen
                            showFullscreen()
                            return false
                        }

                    })
                    .load()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        thread.start()
    }

    override fun onSupportNavigateUp(): Boolean {
//        onBackPressed()
        if (!isRating()) {
            showRate()
        } else {
            finish()
        }

        return true
    }

    override fun onBackPressed() {
//        super.onBackPressed()
//        showRate()
        if (!isRating()) {
            showRate()
        } else {
            finish()
        }
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
            .setDefaultRating(0)
            .setThreshold(3)
            .setTitle("Did you like the app?")
            .setDescription("Let us know what you think")
            .setCommentInputEnabled(true)
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

    fun savePageToRecent(path: String, page: Int) {
        var model = getRecentByPath(path)
        if (!model.isNullOrEmpty()) {
            model[0]?.let { updatePage(it, page) }
        }
    }

    fun updateRecent(model: RecentRealmObject) {
        var realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        model.time = System.currentTimeMillis()
        realm.commitTransaction()
    }

    fun updatePage(model: RecentRealmObject, page: Int) {
        var realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        model.page = page
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
                pdfView.fromUri(fileUri)
                    .enableSwipe(true) // allows to block changing pages using swipe
                    .swipeHorizontal(isSwipeHorizontal)
                    .enableDoubletap(true)
                    .defaultPage(currentPage)
                    .fitEachPage(true)
                    .onPageChange { page: Int, pageCount: Int ->
                        currentPage = page

                        seekBar.progress = page

                    }
                    .onLoad {
                        seekBar.max = it - 1

                    }
                    .enableAnnotationRendering(true) // render annotations (such as comments, colors or forms)
                    .password(password)
                    .scrollHandle(null)
                    .enableAntialiasing(true) // improve rendering a little bit on low-res screens
                    .spacing(10)
                    .onError {
                        if (it is PdfPasswordException) {
                            showInputPassword()
                        }
                    }
                    .onTap(object : OnTapListener {
                        override fun onTap(e: MotionEvent?): Boolean {
                            isFullscreen = !isFullscreen
                            showFullscreen()
                            return false
                        }

                    })
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
        setRatingStatus()
        if (rate >= 4) {
            askRatings()

        } else {
            finish()
        }


        application?.firebaseAnalytics?.logEvent("click_" + rate + "_star", null)
        val params = Bundle()
        params.putString("content", comment)
        application?.firebaseAnalytics?.logEvent("Content_Comment", params)

        application?.firebaseAnalytics?.logEvent("Click_submit", null)

    }

    override fun onPositiveButtonClickedWithoutComment(rate: Int) {
        setRatingStatus()

        application?.firebaseAnalytics?.logEvent("click_" + rate + "_star", null)
        application?.firebaseAnalytics?.logEvent("Click_submit", null)

        if (rate >= 4) {
            askRatings()

        } else {
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

    fun setRatingStatus() {

        var editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit()
        editor.putBoolean("isRating", true)
        editor.apply()
    }

    fun isRating(): Boolean {
        val prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        val isRating = prefs.getBoolean("isRating", false)

        return isRating
    }

    private var mInterstitialAd: InterstitialAd? = null

    private fun prepareAds() {
        preloadAdsLayout.visibility = View.VISIBLE
        MobileAds.initialize(
            this
        ) { initializationStatus: InitializationStatus? -> }

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd!!.adUnitId = Constants.ADMOB_Interstitial_Click_Open_Item
        mInterstitialAd!!.adListener = mDefaultListener
        val adRequest = AdRequest.Builder().build()
        mInterstitialAd!!.loadAd(adRequest)
    }

    private val mDefaultListener: AdListener = object : AdListener() {
        override fun onAdLoaded() {
            super.onAdLoaded()
            if (mInterstitialAd != null && mInterstitialAd!!.isLoaded) {

                mInterstitialAd!!.show()
                preloadAdsLayout.visibility = View.GONE

            }
        }

        override fun onAdClosed() {

        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            super.onAdFailedToLoad(loadAdError)
            preloadAdsLayout.visibility = View.GONE

        }
    }

    override fun onPause() {
        super.onPause()
        if (viewType == 0) {
            url?.let { savePageToRecent(it, currentPage) }
        }
    }

    fun showInputPassword() {

        val view = layoutInflater.inflate(R.layout.dialog_input_password, null)
        val categoryEditText = view.findViewById(R.id.categoryEditText) as EditText
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Password protect")
            .setMessage("Input password to view file")
            .setView(view)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                val text = categoryEditText.text.toString()
                password = text
                if (viewType == 0) {
                    viewFile()
                } else {
                    viewFileFromStream()
                }
            })
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    fun showFullscreen() {
        if (isFullscreen) {
            hideStatus()
            toolbar.visibility = View.GONE
            bottom.visibility = View.GONE
        } else {
            showStatus()
            toolbar.visibility = View.VISIBLE
            bottom.visibility = View.VISIBLE

        }
    }

    fun hideStatus() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }
    }

    fun showStatus() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.show(WindowInsets.Type.statusBars())
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        }
    }

    fun onMoreClick(view: View) {
        //Creating the instance of PopupMenu
        val popup = PopupMenu(this, view)

        popup.menuInflater
            .inflate(R.menu.poupup_reader, popup.menu)

        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {
                when (item?.itemId) {
                    R.id.printer -> {
                        var printManager: PrintManager =
                            getSystemService(Context.PRINT_SERVICE) as PrintManager
                        try {
                            var printAdapter =
                                PdfDocumentAdapter(this@PdfViewerActivity, url)

                            printManager.print(
                                "Document",
                                printAdapter,
                                PrintAttributes.Builder().build()
                            );
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    R.id.favorite -> {

                    }

                    R.id.gotoPage -> {
                        showInputPage()
                    }

                    R.id.upload -> {

                    }
                }

                return true

            }

        })

        popup.show() //showing popup menu

    }

    fun showInputPage() {
        val view = layoutInflater.inflate(R.layout.dialog_input_name, null)
        val categoryEditText = view.findViewById(R.id.categoryEditText) as EditText

        categoryEditText.inputType = InputType.TYPE_CLASS_NUMBER

        val dialog: android.app.AlertDialog =
            android.app.AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("Input page number")
                .setView(view)
                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    onChangePage(categoryEditText.text.toString())

                })
                .setNegativeButton("Cancel", null)
                .create()
        dialog.show()
    }

    fun onChangePage(page: String) {
        if (!TextUtils.isEmpty(page)) {
            pdfView.jumpTo(page.toInt(), true)
        }
    }

//    private fun saveFiletoDrive(file: File, mime: String) {
//        // Start by creating a new contents, and setting a callback.
//        Drive.DriveApi.newDriveContents(mDriveClient).setResultCallback(
//            object : ResultCallback<DriveContentsResult?>() {
//                fun onResult(result: DriveContentsResult) {
//                    // If the operation was not successful, we cannot do
//                    // anything
//                    // and must
//                    // fail.
//                    if (!result.getStatus().isSuccess()) {
//                        Log.i(TAG, "Failed to create new contents.")
//                        return
//                    }
//                    Log.i(TAG, "Connection successful, creating new contents...")
//                    // Otherwise, we can write our data to the new contents.
//                    // Get an output stream for the contents.
//                    var outputStream: OutputStream? = result.getDriveContents()
//                        .getOutputStream()
//                    var fis: FileInputStream?
//                    try {
//                        fis = FileInputStream(file.path)
//                        val baos = ByteArrayOutputStream()
//                        val buf = ByteArray(1024)
//                        var n: Int
//                        while (-1 != fis.read(buf).also { n = it }) baos.write(buf, 0, n)
//                        val photoBytes: ByteArray = baos.toByteArray()
//                        outputStream.write(photoBytes)
//                        outputStream.close()
//                        outputStream = null
//                        fis.close()
//                        fis = null
//                    } catch (e: FileNotFoundException) {
//                        Log.w(TAG, "FileNotFoundException: " + e.getMessage())
//                    } catch (e1: IOException) {
//                        Log.w(TAG, "Unable to write file contents." + e1.getMessage())
//                    }
//                    val title = file.name
//                    val metadataChangeSet: MetadataChangeSet = Builder()
//                        .setMimeType(mime).setTitle(title).build()
//                    if (mime == MIME_PHOTO) {
//                        if (VERBOSE) Log.i(
//                            TAG, "Creating new photo on Drive (" + title
//                                    + ")"
//                        )
//                        Drive.DriveApi.getFolder(
//                            mDriveClient,
//                            mPicFolderDriveId
//                        ).createFile(
//                            mDriveClient,
//                            metadataChangeSet,
//                            result.getDriveContents()
//                        )
//                    } else if (mime == MIME_VIDEO) {
//                        Log.i(
//                            TAG, "Creating new video on Drive (" + title
//                                    + ")"
//                        )
//                        Drive.DriveApi.getFolder(
//                            mDriveClient,
//                            mVidFolderDriveId
//                        ).createFile(
//                            mDriveClient,
//                            metadataChangeSet,
//                            result.getDriveContents()
//                        )
//                    }
//                    if (file.delete()) {
//                        if (VERBOSE) Log.d(TAG, "Deleted " + file.name + " from sdcard")
//                    } else {
//                        Log.w(TAG, "Failed to delete " + file.name + " from sdcard")
//                    }
//                }
//            })
//    }

}