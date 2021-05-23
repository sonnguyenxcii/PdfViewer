package py.com.opentech.drawerwithbottomnavigation.ui.pdf

import android.annotation.SuppressLint
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnTapListener
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.hosseiniseyro.apprating.AppRatingDialog
import com.hosseiniseyro.apprating.listener.RatingDialogListener
import com.shockwave.pdfium.PdfPasswordException
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.include_preload_ads.*
import py.com.opentech.drawerwithbottomnavigation.BuildConfig
import py.com.opentech.drawerwithbottomnavigation.HomeActivity
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.realm.BookmarkRealmObject
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
    var isBookmark = false

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
                    url = file_uri.path
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

        var model = url?.let { getBookmarkByPath(it) }

        if (!model.isNullOrEmpty()) {
            isBookmark = true
            invalidateOptionsMenu()
        }


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
            onFinishing()
        }

        return true
    }

    override fun onBackPressed() {
//        super.onBackPressed()
//        showRate()
        if (!isRating()) {
            showRate()
        } else {
            onFinishing()
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
            model[0]?.let { updatePage(it, page, pdfView.pageCount) }
        }
    }

    fun updateRecent(model: RecentRealmObject) {
        var realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        model.time = System.currentTimeMillis()
        realm.commitTransaction()
    }

    fun updatePage(model: RecentRealmObject, page: Int, totalPage: Int) {
        var realm = Realm.getDefaultInstance()
        realm.beginTransaction()
        model.page = page
        model.totalPage = totalPage
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
        onFinishing()
    }

    override fun onNeutralButtonClicked() {
        onFinishing()

    }

    override fun onPositiveButtonClickedWithComment(rate: Int, comment: String) {

        if (rate < 1) {
            showRateInvalidDialog()
            return
        }
        setRatingStatus()
        if (rate >= 4) {
            askRatings()

        } else {
            onFinishing()
        }


        application?.firebaseAnalytics?.logEvent("click_" + rate + "_star", null)
        val params = Bundle()
        params.putString("content", comment)
        application?.firebaseAnalytics?.logEvent("Content_Comment", params)

        application?.firebaseAnalytics?.logEvent("Click_submit", null)

    }

    override fun onPositiveButtonClickedWithoutComment(rate: Int) {

        if (rate < 1) {
            showRateInvalidDialog()
            return
        }

        setRatingStatus()

        application?.firebaseAnalytics?.logEvent("click_" + rate + "_star", null)
        application?.firebaseAnalytics?.logEvent("Click_submit", null)

        if (rate >= 4) {
            askRatings()

        } else {
            onFinishing()
        }
    }

    fun onFinishing(){
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }

    fun showRateInvalidDialog() {
        if (isFinishing) {
            return
        }
        AlertDialog.Builder(this)
            .setTitle("")
            .setMessage("You need to make a rate")
            .setPositiveButton("OK", null).show()
    }

    fun askRatings() {
        try {
            val url =
                "https://play.google.com/store/apps/details?id=com.pdfreader.scanner.pdfviewer"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        } catch (e: Exception) {

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

        try {
            val currentFile = File(url)
            var fileName = currentFile.nameWithoutExtension
            val params = Bundle()
            params.putString("file_name", fileName)
            params.putString("file_page_count", "" + pdfView.pageCount)
            application?.firebaseAnalytics?.logEvent("File_View_Info", params)
        } catch (e: Exception) {

        }

    }

    fun showInputPassword() {

        val view = layoutInflater.inflate(R.layout.dialog_input_password, null)
        val categoryEditText = view.findViewById(R.id.categoryEditText) as EditText
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Password protect")
            .setMessage("Input password to view file")
            .setView(view)
            .setPositiveButton("OK") { dialog, which ->
                val text = categoryEditText.text.toString()
                password = text
                if (viewType == 0) {
                    viewFile()
                } else {
                    viewFileFromStream()
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                onFinishing()
            }
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

    @SuppressLint("RestrictedApi")
    fun onMoreClick(view: View) {
        //Creating the instance of PopupMenu
//        val popup = PopupMenu(this, view)
//
//        popup.menuInflater
//            .inflate(R.menu.poupup_reader, popup.menu)

        val menuBuilder = MenuBuilder(this)
        val inflater = MenuInflater(this)
        inflater.inflate(R.menu.poupup_reader, menuBuilder)
        val optionsMenu = MenuPopupHelper(this, menuBuilder, view)
        optionsMenu.setForceShowIcon(true)

        var menuItem = menuBuilder.getItem(1)

        if (isBookmark) {
            menuItem.title = "Remove from bookmark"
        } else {
            menuItem.title = "Bookmark"

        }

        menuBuilder.setCallback(object : MenuBuilder.Callback {
            override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean {
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
                        onBookmarkClick()
                    }

                    R.id.gotoPage -> {
                        showInputPage()
                    }

                    R.id.upload -> {
                        url?.let { onFileClick(it) }
                    }
                }

                return true

            }

            override fun onMenuModeChange(menu: MenuBuilder) {
            }

        })

        optionsMenu.show() //showing popup menu



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

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {

        // set your desired icon here based on a flag if you like
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val settingsItem = menu?.findItem(R.id.favorite)
        if (isBookmark) {
            settingsItem?.title = "Remove from Bookmark"
        } else {
            settingsItem?.title = "Bookmark"

        }
        return super.onCreateOptionsMenu(menu)
    }

    fun getBookmarkByPath(path: String): List<BookmarkRealmObject?>? {
        var realm = Realm.getDefaultInstance()
        return realm.where(BookmarkRealmObject::class.java).equalTo("path", path).findAll()
    }

    fun onBookmarkClick() {

//        var bookmarkStatus = data.isBookmark!!
        val params = Bundle()

        if (isBookmark) {
            params.putString("bookmark_file", "0")

            url?.let { deleteFromBookmark(it) }
        } else {
            params.putString("bookmark_file", "1")

            url?.let { addToBookmark(it) }
        }

        application?.firebaseAnalytics?.logEvent("PDF_Viewer_Layout", params)

        isBookmark = !isBookmark
        invalidateOptionsMenu()
    }

    fun deleteFromBookmark(path: String) {
        var realm = Realm.getDefaultInstance()

        realm.executeTransaction { realm ->
            val result: RealmResults<BookmarkRealmObject> =
                realm.where(BookmarkRealmObject::class.java).equalTo("path", path).findAll()
            result.deleteAllFromRealm()
        }
    }

    fun addToBookmark(path: String) {
        var model = getBookmarkByPath(path)
        if (model.isNullOrEmpty()) {
            saveBookmark(path)
        }
    }

    fun saveBookmark(path: String) {
        var realm = Realm.getDefaultInstance()
        var id = PdfApplication.bookmarkPrimaryKey.getAndIncrement();

        realm.executeTransactionAsync { realm ->
            val model: BookmarkRealmObject =
                realm?.createObject(BookmarkRealmObject::class.java, id)!!
            model.path = path
        }
    }

    private fun onFileClick(path: String) {
        try {
            val AUTHORITY_APP = "com.pdfreader.scanner.pdfviewer.provider"
            val uri = FileProvider.getUriForFile(this, AUTHORITY_APP, File(path))
            val uris: ArrayList<Uri> = ArrayList()
            uris.add(uri)
            val intent = Intent()
            intent.action = Intent.ACTION_SEND_MULTIPLE
            intent.putExtra(Intent.EXTRA_TEXT, "Upload PDF file")
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = "application/pdf"
            intent.setPackage("com.google.android.apps.docs")


            try {
                startActivity(
                    Intent.createChooser(
                        intent,
                        "Select app"
                    )
                )
            } catch (e: java.lang.Exception) {
                Toast.makeText(this, "Can not share file now.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {

        }

    }
}