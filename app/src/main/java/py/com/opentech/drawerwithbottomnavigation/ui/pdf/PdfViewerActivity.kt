package py.com.opentech.drawerwithbottomnavigation.ui.pdf

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPopupHelper
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.FileProvider
import com.ads.control.Admod
import com.ads.control.funtion.AdCallback
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.listener.OnTapListener
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.shockwave.pdfium.PdfPasswordException
import com.willy.ratingbar.ScaleRatingBar
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.include_preload_ads.*
import py.com.opentech.drawerwithbottomnavigation.BuildConfig
import py.com.opentech.drawerwithbottomnavigation.HomeActivity
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.realm.BookmarkRealmObject
import py.com.opentech.drawerwithbottomnavigation.model.realm.RecentRealmObject
import py.com.opentech.drawerwithbottomnavigation.ui.component.CustomRatingDialogListener
import py.com.opentech.drawerwithbottomnavigation.utils.CommonUtils
import py.com.opentech.drawerwithbottomnavigation.utils.Constants
import py.com.opentech.drawerwithbottomnavigation.utils.Constants.MY_PREFS_NAME
import py.com.opentech.drawerwithbottomnavigation.utils.OnSingleClickListener
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.*
import kotlin.math.roundToInt


class PdfViewerActivity : AppCompatActivity(), CustomRatingDialogListener {

    var isSwipeHorizontal = false
    lateinit var pdfView: PDFView
    lateinit var seekBar: AppCompatSeekBar
    lateinit var rotate: AppCompatImageView
    lateinit var nightMode: AppCompatImageView
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
    var isNightMode = false
    var isPasswordProtect = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)
        Admod.getInstance().loadBanner(this, "ca-app-pub-3617606523175567/4292503925");

        pdfView = findViewById(R.id.pdfView)
        toolbar = findViewById(R.id.toolbar)
        rotate = findViewById(R.id.rotate)
        nightMode = findViewById(R.id.night_mode)
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
            application = PdfApplication.create(this)

            if (null != intent.data) {
                viewType = 1
                try {
                    val params = Bundle()
                    application?.firebaseAnalytics?.logEvent("Open_From_Other_App", params)
                } catch (e: Exception) {
                }
                prepareAds()

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
                                currentPage = 0
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
            if (!isSwipeHorizontal) {
                rotate.setImageResource(R.drawable.ic_horizontal)
            } else {
                rotate.setImageResource(R.drawable.ic_vertical)

            }
            if (viewType == 0) {
                viewFile()
            } else {
                viewFileFromStream()
            }
        }

        nightMode.setOnClickListener {
            isNightMode = !isNightMode
            if (!isNightMode) {
                nightMode.setImageResource(R.drawable.ic_night_shift)
            } else {
                nightMode.setImageResource(R.drawable.ic_brightness)

            }
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
                    try {
                        shareFileUri(it)
                    } catch (e: Exception) {
                    }
                }
            }
        })

        more.setOnClickListener(object : OnSingleClickListener() {
            override fun onSingleClick(v: View?) {
                v?.let { onMoreClick(it) }
            }
        })

        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
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

    fun isFromOtherApp(): Boolean {
        return viewType != 0
    }

    private fun getFilePathForN(uri: Uri, context: Context): String? {
        var path = ""
        try {
            val returnCursor: Cursor =
                context.getContentResolver().query(uri, null, null, null, null)!!
            /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
            val nameIndex: Int = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val sizeIndex: Int = returnCursor.getColumnIndex(OpenableColumns.SIZE)
            returnCursor.moveToFirst()
            val name: String = returnCursor.getString(nameIndex)
            val size = java.lang.Long.toString(returnCursor.getLong(sizeIndex))
            val file: File = File(context.getFilesDir(), name)

            val inputStream: InputStream = context.getContentResolver().openInputStream(uri)!!
            val outputStream = FileOutputStream(file)
            var read = 0
            val maxBufferSize = 1 * 1024 * 1024
            val bytesAvailable: Int = inputStream.available()

            //int bufferSize = 1024;
            val bufferSize = Math.min(bytesAvailable, maxBufferSize)
            val buffers = ByteArray(bufferSize)
            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }
            Log.e("File Size", "Size " + file.length())
            inputStream.close()
            outputStream.close()
            Log.e("File Path", "Path " + file.path)
            Log.e("File Size", "Size " + file.length())
            path = file.path
        } catch (e: Exception) {
            e.message?.let { Log.e("Exception", it) }
        }
        return path
    }

    private fun uriToPath(uri: Uri): String {
        val backupFile = File(uri.path)
        val absolutePath = backupFile.absolutePath
        return absolutePath.substring(absolutePath.indexOf(':') + 1)
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
                            isPasswordProtect = true
                            showInputPassword()
                        }
                    }
                    .nightMode(isNightMode)
                    .onTap(object : OnTapListener {
                        override fun onTap(e: MotionEvent?): Boolean {
                            isFullscreen = !isFullscreen
                            showFullscreen()
                            return false
                        }

                    })
                    .load()
            } catch (e: Exception) {
            }
        }
        thread.start()
    }

    override fun onSupportNavigateUp(): Boolean {
        prepareToFinish()
        return true
    }

    fun prepareToFinish() {
        if (isFromOtherApp()) {
            onFinishing()
        } else {
            if (!isRating()) {
                showRate()
            } else {
                onFinishing()
            }
        }
    }

    override fun onBackPressed() {

        prepareToFinish()
    }

    fun showRate() {
        val view = layoutInflater.inflate(R.layout.dialog_rating, null)
        val submit = view.findViewById(R.id.submit) as View
        val skip = view.findViewById(R.id.skip) as View
        val ratingBar = view.findViewById(R.id.ratingBar) as ScaleRatingBar
        val commentEditText = view.findViewById(R.id.commentEditText) as AppCompatEditText

        ratingBar.setOnRatingChangeListener { ratingBar, rating, fromUser ->

            if (rating <= 0) {
                skip.visibility = View.VISIBLE
                submit.visibility = View.GONE
            } else {
                skip.visibility = View.GONE
                submit.visibility = View.VISIBLE
            }

            if (rating <= 3) {
                commentEditText.visibility = View.VISIBLE
            } else {
                commentEditText.setText("")
                commentEditText.visibility = View.GONE
            }
        }

        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()
        Objects.requireNonNull(dialog.window)
            ?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        skip.setOnClickListener {
            dialog.dismiss()
            onFinishing()

        }

        submit.setOnClickListener {
            if (!TextUtils.isEmpty(commentEditText.text.toString())) {
                onPositiveButtonClickedWithComment(
                    ratingBar.rating.roundToInt(),
                    commentEditText.text.toString()
                )
            } else {
                onPositiveButtonClickedWithoutComment(ratingBar.rating.roundToInt())

            }
            dialog.dismiss()
        }
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
        try {
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
        } catch (e: Exception) {

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
                        println("------------------" + it.message)
                        if (it is PdfPasswordException) {
                            isPasswordProtect = true

                            showInputPassword()
                        }
                    }.nightMode(isNightMode)
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

    override fun onNoneChoose() {
        showRateInvalidDialog()

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
            Toast.makeText(this, "Thank for rating", Toast.LENGTH_SHORT).show()

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
            Toast.makeText(this, "Thank for rating", Toast.LENGTH_SHORT).show()

            onFinishing()
        }
    }

    fun onFinishing() {
        if (isFromOtherApp()) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            super.onBackPressed()
        }

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
        val prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE)
        val time = prefs.getLong("openFromOtherAppTimeOut", 5000)
        Admod.getInstance().loadSplashInterstitalAds(this,
            Constants.ADMOB_Iterstitial_Open_From_Other_App,
            time,
            object : AdCallback() {

                override fun onAdClosed() {
                    preloadAdsLayout.visibility = View.GONE

                    initData()
                }

                override fun onAdFailedToLoad(i: Int) {
                    preloadAdsLayout.visibility = View.GONE

                    initData()
                }
            })
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
            initData()
        }

        override fun onAdFailedToLoad(loadAdError: LoadAdError) {
            super.onAdFailedToLoad(loadAdError)
            preloadAdsLayout.visibility = View.GONE
            initData()
        }
    }

    fun initData() {
        val file_uri = intent.data

        if (file_uri != null) {
            fileUri = file_uri
            viewFileFromStream()

            url = getFilePathForN(fileUri!!, this)

        }
    }

    override fun onPause() {
        super.onPause()
        if (viewType == 0) {
            url?.let { savePageToRecent(it, currentPage) }
        }

//        try {
//            val currentFile = File(url)
//            var fileName = currentFile.nameWithoutExtension
//            val params = Bundle()
//            params.putString("file_name", fileName)
//            params.putString("file_page_count", "" + pdfView.pageCount)
//            application?.firebaseAnalytics?.logEvent("File_View_Info", params)
//        } catch (e: Exception) {
//
//        }

    }

    fun showInputPassword() {
        try {
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
        }catch (e:Exception){

        }

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
                when (item.itemId) {
                    R.id.printer -> {
                        println("-printer-------------url---------" + url)
//                        if (isPasswordProtect) {
//
//                        } else {
                        url?.let { CommonUtils.onActionPrint(this@PdfViewerActivity, it) }
//                        }
                    }

                    R.id.favorite -> {
                        onBookmarkClick()
                    }

                    R.id.gotoPage -> {
                        showInputPage()
                    }

                    R.id.upload -> {
                        println("-upload-------------url---------" + url)
                        url?.let {
                            CommonUtils.onFileClick(this@PdfViewerActivity, it)

                        }
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

        val maxLength = 10
        categoryEditText.filters = arrayOf<InputFilter>(LengthFilter(maxLength))

        val dialog: android.app.AlertDialog =
            android.app.AlertDialog.Builder(this)
                .setTitle("")
                .setMessage("Input page number")
                .setView(view)
                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    var number = 0
                    try {
                        number = categoryEditText.text.toString().toInt()
                    } catch (e: Exception) {
                        number = -1
                    }
                    if (number == -1 || number > pdfView.pageCount) {
                        Toast.makeText(this, " Not valid page number", Toast.LENGTH_SHORT)
                            .show()
                    } else {
                        onChangePage(number)

                    }

                })
                .setNegativeButton("Cancel", null)
                .create()
        dialog.show()
    }

    fun onChangePage(page: Int) {
        try {
            pdfView.jumpTo(page, true)

        } catch (e: Exception) {
            e.printStackTrace()
        }
//        if (!TextUtils.isEmpty(page)) {
//        }
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


}