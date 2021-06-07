package py.com.opentech.drawerwithbottomnavigation

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.provider.Settings
import android.text.TextUtils
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.RadioButton
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.ads.control.Admod
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.navigation.NavigationView
import com.infideap.drawerbehavior.AdvanceDrawerLayout
import com.willy.ratingbar.ScaleRatingBar
import kotlinx.android.synthetic.main.app_bar_default.*
import kotlinx.android.synthetic.main.include_preload_ads.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import py.com.opentech.drawerwithbottomnavigation.model.FileChangeEvent
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import py.com.opentech.drawerwithbottomnavigation.model.SortModel
import py.com.opentech.drawerwithbottomnavigation.ui.component.CustomRatingDialogListener
import py.com.opentech.drawerwithbottomnavigation.ui.imagetopdf.ImageToPdfActivity
import py.com.opentech.drawerwithbottomnavigation.ui.merge.MergePdfActivity
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfViewerActivity
import py.com.opentech.drawerwithbottomnavigation.utils.Constants
import py.com.opentech.drawerwithbottomnavigation.utils.InternetConnection
import py.com.opentech.drawerwithbottomnavigation.utils.RealPathUtil
import py.com.opentech.drawerwithbottomnavigation.utils.Utils
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt


class HomeActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener, CustomRatingDialogListener {

    private var drawer: AdvanceDrawerLayout? = null
    private lateinit var navController: NavController
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    protected var application: PdfApplication? = null
    private var mInterstitialAd: InterstitialAd? = null
    private var mInterstitialFileAd: InterstitialAd? = null
    private var mInterstitialMergeAd: InterstitialAd? = null
    private var adRequest: AdRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_layout)
        application = PdfApplication.create(this)

        adRequest = AdRequest.Builder().build()
        var sortData = getSortStatus()
        application?.global?.sortData?.postValue(sortData)

        this.application?.global?.isListMode?.postValue(true)

        setupNavController()

        Admod.getInstance().loadSmallNative(this, Constants.ADMOB_Native_Bottom_Left_Menu)

        drawer = findViewById<View>(R.id.drawer_layout) as AdvanceDrawerLayout

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        drawer!!.setViewScale(Gravity.START, 0.9f)
        drawer!!.setRadius(Gravity.START, 0f)
        drawer!!.setViewElevation(Gravity.START, 20f)

        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(1).isEnabled = false

//        var badge = bottomNavigationView.getOrCreateBadge(R.id.recent)
//        badge.isVisible = true
//        badge.number = 9

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.recent) {

                val params = Bundle()
                params.putString("button_click", "Recently")
                application?.firebaseAnalytics?.logEvent("Home_Layout", params)
//
//                if (InternetConnection.checkConnection(this)) {
//                    Admod.getInstance().forceShowInterstitial(
//                        this,
//                        application?.mInterstitialClickTabAd,
//                        object : AdCallback() {
//                            override fun onAdClosed() {
//                                navigateTo(item.itemId)
//                            }
//                        }
//                    )
//                } else {
                navigateTo(item.itemId)
//                }
            } else if (item.itemId == R.id.nav_home) {

                val params = Bundle()
                params.putString("button_click", "Folder")
                application?.firebaseAnalytics?.logEvent("Home_Layout", params)

                navigateTo(item.itemId)
            } else {
                navigateTo(item.itemId)

            }

            true
        }

        homeBtn.setOnClickListener {
            if (drawer!!.isDrawerOpen(GravityCompat.START)) {
                drawer!!.closeDrawer(GravityCompat.START)
            } else {
                drawer!!.openDrawer(GravityCompat.START)

            }
        }

        fab.setOnClickListener {
            val params = Bundle()
            params.putString("button_click", "Bookmark")
            application?.firebaseAnalytics?.logEvent("Home_Layout", params)
//            if (InternetConnection.checkConnection(this)) {
//                Admod.getInstance().forceShowInterstitial(
//                    this,
//                    application?.mInterstitialClickTabAd,
//                    object : AdCallback() {
//                        override fun onAdClosed() {
            navigateToBookmark()
//                        }
//                    }
//                )
//            } else {
//                navigateToBookmark()
//            }
        }

        mode.setOnClickListener {
            var temp = this.application?.global?.isListMode?.value
            this.application?.global?.isListMode?.postValue(!temp!!)
        }

        sort.setOnClickListener {
            showInputSort()
        }

        search.setOnClickListener {
            val params = Bundle()
            params.putString("button_click", "Button Search")
            application?.firebaseAnalytics?.logEvent("Home_Layout", params)

            if (application?.mInterstitialSearchAd == null) {
                application?.mInterstitialSearchAd = Admod.getInstance()
                    .getInterstitalAds(this, Constants.ADMOB_Interstitial_Search)
            }

            var intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        application?.global?.isListMode?.observe(this, Observer {
            if (it != null) {
                if (it) {
                    mode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mode_grid))
                } else {
                    mode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mode_list))

                }
            }
        })


    }

    fun navigateToBookmark() {
        navigateTo(R.id.nav_bookmark)
        bottomNavigationView.selectedItemId = R.id.nav_bookmark
    }

    private fun setupNavController() {
        navController = findNavController(R.id.nav_host_fragment)
    }

    private fun navigateTo(resId: Int) {
        navController.navigate(resId)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(noteEvent: FileChangeEvent) {
        requestRead()

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        requestRead()

    }

    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawer(GravityCompat.START)
        } else {
            finish()
//            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.space1 -> {
//                Toast.makeText(this, "file", Toast.LENGTH_SHORT).show()
//                drawer!!.openDrawer(GravityCompat.END)
                drawer!!.closeDrawer(GravityCompat.START)
                return false

            }
            R.id.nav_home -> {
//                Toast.makeText(this, "file", Toast.LENGTH_SHORT).show()
//                drawer!!.openDrawer(GravityCompat.END)
                drawer!!.closeDrawer(GravityCompat.START)
                return false

            }
            R.id.nav_file_manager -> {

                val params = Bundle()
                params.putString("menu_click", "File Management")
                application?.firebaseAnalytics?.logEvent("Menu_Layout", params)


                processToFile()

                drawer!!.closeDrawer(GravityCompat.START)
                return false

            }
            R.id.nav_pdf_scan -> {

                val params = Bundle()
                params.putString("menu_click", "PDF Scanner")
                application?.firebaseAnalytics?.logEvent("Menu_Layout", params)

                if (!InternetConnection.checkConnection(this)) {
                    navigateToScan()

                } else {
                    preloadAdsLayout.visibility = View.VISIBLE

                    InterstitialAd.load(this, Constants.ADMOB_Interstitial_Pdf_Scanner,
                        adRequest!!, object : InterstitialAdLoadCallback() {
                            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                                // The mInterstitialAd reference will be null until
                                // an ad is loaded.
                                mInterstitialAd = interstitialAd

                                mInterstitialAd?.fullScreenContentCallback =
                                    object : FullScreenContentCallback() {
                                        override fun onAdDismissedFullScreenContent() {
                                            println("----onAdDismissedFullScreenContent---------------")
                                            // Called when fullscreen content is dismissed.
                                            preloadAdsLayout.visibility = View.GONE
                                            mInterstitialAd = null
                                            navigateToScan()

                                        }

                                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                            // Called when fullscreen content failed to show.
                                            println("----onAdFailedToShowFullScreenContent---------------")
                                            preloadAdsLayout.visibility = View.GONE
                                            navigateToScan()

                                        }

                                        override fun onAdShowedFullScreenContent() {
                                            preloadAdsLayout.visibility = View.GONE
                                            // Called when fullscreen content is shown.
                                            // Make sure to set your reference to null so you don't
                                            // show it a second time.
                                            mInterstitialAd = null

                                        }
                                    }

                                mInterstitialAd!!.show(this@HomeActivity)

                            }

                            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                                // Handle the error
                                mInterstitialAd = null
                                preloadAdsLayout.visibility = View.GONE
                                navigateToScan()

                            }
                        })
                }


                drawer!!.closeDrawer(GravityCompat.START)
                return false
            }

            R.id.nav_pdf_merge -> {

                val params = Bundle()
                params.putString("menu_click", "Merge PDF")
                application?.firebaseAnalytics?.logEvent("Menu_Layout", params)
                processToMerge()
//                if (!InternetConnection.checkConnection(this)) {
//                    navigateToMerge()
//
//                } else {
//                    var isEarn = false
//                    Admod.getInstance().loadVideoAds(this, object : RewardedAdCallback() {
//                        override fun onUserEarnedReward(p0: RewardItem) {
//                            isEarn = true
//                        }
//
//                        override fun onRewardedAdClosed() {
//                            super.onRewardedAdClosed()
//                            if (isEarn) {
//                                navigateToMerge()
//                            }
//
//                        }
//
//                        override fun onRewardedAdFailedToShow(p0: AdError?) {
//                            super.onRewardedAdFailedToShow(p0)
//                            navigateToMerge()
//
//                        }
//                    })
//                }


                drawer!!.closeDrawer(GravityCompat.START)
                return false
            }

            R.id.nav_feedback -> {
                composeEmail()
//                Toast.makeText(this, "feedback", Toast.LENGTH_SHORT).show()
                drawer!!.closeDrawer(GravityCompat.START)
                return false
            }
            R.id.nav_share -> {
                shareApp()
//                Toast.makeText(this, "share", Toast.LENGTH_SHORT).show()
                drawer!!.closeDrawer(GravityCompat.START)
                return false
            }
            R.id.nav_rating -> {
                showRate()
//                shareApp()
//                Toast.makeText(this, "share", Toast.LENGTH_SHORT).show()
                drawer!!.closeDrawer(GravityCompat.START)
                return false
            }
            R.id.nav_language -> {
                showLanguageDialog()
//                Toast.makeText(this, "file", Toast.LENGTH_SHORT).show()
//                drawer!!.openDrawer(GravityCompat.END)
                drawer!!.closeDrawer(GravityCompat.START)
                return false

            }
        }
//        drawer!!.closeDrawer(GravityCompat.START)
        return true
    }

    fun processToFile() {

        if (!InternetConnection.checkConnection(this)) {
            navigateToFile()

        } else {
            preloadAdsLayout.visibility = View.VISIBLE

            InterstitialAd.load(this, Constants.ADMOB_Interstitial_File_Management,
                adRequest!!, object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialFileAd = interstitialAd

                        mInterstitialFileAd?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    println("----onAdDismissedFullScreenContent---------------")
                                    // Called when fullscreen content is dismissed.
                                    preloadAdsLayout.visibility = View.GONE
                                    mInterstitialFileAd = null
                                    navigateToFile()

                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    // Called when fullscreen content failed to show.
                                    println("----onAdFailedToShowFullScreenContent---------------")
                                    preloadAdsLayout.visibility = View.GONE
                                    navigateToFile()

                                }

                                override fun onAdShowedFullScreenContent() {
                                    preloadAdsLayout.visibility = View.GONE
                                    mInterstitialFileAd = null

                                }
                            }

                        mInterstitialFileAd!!.show(this@HomeActivity)

                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        mInterstitialFileAd = null
                        preloadAdsLayout.visibility = View.GONE
                        navigateToFile()

                    }
                })
        }
    }

    fun processToMerge() {

        if (!InternetConnection.checkConnection(this)) {
            navigateToMerge()
        } else {
            preloadAdsLayout.visibility = View.VISIBLE

            InterstitialAd.load(this, Constants.ADMOB_Interstitial_Merge_PDF,
                adRequest!!, object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialMergeAd = interstitialAd

                        mInterstitialMergeAd?.fullScreenContentCallback =
                            object : FullScreenContentCallback() {
                                override fun onAdDismissedFullScreenContent() {
                                    println("----onAdDismissedFullScreenContent---------------")
                                    // Called when fullscreen content is dismissed.
                                    preloadAdsLayout.visibility = View.GONE
                                    mInterstitialMergeAd = null
                                    navigateToMerge()

                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    // Called when fullscreen content failed to show.
                                    println("----onAdFailedToShowFullScreenContent---------------")
                                    preloadAdsLayout.visibility = View.GONE
                                    navigateToMerge()

                                }

                                override fun onAdShowedFullScreenContent() {
                                    preloadAdsLayout.visibility = View.GONE
                                    mInterstitialMergeAd = null

                                }
                            }

                        mInterstitialMergeAd!!.show(this@HomeActivity)

                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        mInterstitialMergeAd = null
                        preloadAdsLayout.visibility = View.GONE
                        navigateToMerge()

                    }
                })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateToScan() {

        var intent = Intent(this, ImageToPdfActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMerge() {

        var intent = Intent(this, MergePdfActivity::class.java)
        startActivity(intent)
    }

    private val INTENT_REQUEST_PICK_FILE_CODE = 10

    private fun navigateToFile() {
        val folderPath = Environment.getExternalStorageDirectory().toString() + "/"
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        val myUri = Uri.parse(folderPath)
        intent.setDataAndType(myUri, "application/pdf")


//        var intent = Intent(this, FileExplorerActivity::class.java)
//        startActivity(intent)
        startActivityForResult(
            Intent.createChooser(intent, "Select a file"), INTENT_REQUEST_PICK_FILE_CODE
        )
    }


    private fun getExternalPDFFileList(): ArrayList<PdfModel> {
        var uriList: ArrayList<PdfModel> = ArrayList()

//        uriList.clear()
//        walkDir(Environment.getExternalStorageDirectory())

        println("--uriList----------------" + uriList.size)
        try {
            val ROOT_DIR = Environment.getExternalStorageDirectory().absolutePath
            println("--ROOT_DIR------" + ROOT_DIR)
            val ANDROID_DIR = File("$ROOT_DIR/Android")
            val DATA_DIR = File("$ROOT_DIR/data")
            File(ROOT_DIR).walk()
                // befor entering this dir check if
//                .onEnter {
//                    !it.isHidden // it is not hidden
//                    it != ANDROID_DIR // it is not Android directory
//                            && it != DATA_DIR // it is not data directory
//                            && !File(it, ".nomedia").exists() //there is no .nomedia file inside
//                }
//                .filter { it.extension == "pdf" }
                .toList().forEach {
                    if (it.name.toLowerCase().endsWith(".pdf")) {
                        val lastModDate = Date(it.lastModified())
                        var parentName = ""
                        try {
                            parentName = File(it.parent).name
                        } catch (e: Exception) {

                        }
                        uriList.add(
                            PdfModel(
                                name = it.name,
                                path = it.absolutePath,
                                size = it.length(),
                                date = Utils.formatDate(lastModDate),
                                folder = parentName,
                                lastModifier = it.lastModified()
                            )
                        )
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return uriList
    }


    fun walkDir(dir: File) {
        var listFile = dir.listFiles()
        listFile?.forEach {
            if (it.isDirectory) {
                walkDir(it)
            } else {
                if (it.name.toLowerCase().endsWith(".pdf")) {
                    val lastModDate = Date(it.lastModified())
                    var parentName = ""
                    try {
                        parentName = File(it.parent).name
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    println("-name------------------" + it.name)
                    println("-absolutePath------------------" + it.absolutePath)
//                    uriList.add(
//                        PdfModel(
//                            name = it.name,
//                            path = it.absolutePath,
//                            size = it.length(),
//                            date = Utils.formatDate(lastModDate),
//                            folder = parentName,
//                            lastModifier = it.lastModified()
//                        )
//                    )
                }
            }
        }
    }

    fun requestRead() {

        if (checkPermission()) {
            readFile()

        } else {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    intent.addCategory("android.intent.category.DEFAULT")
                    intent.data =
                        Uri.parse(String.format("package:%s", applicationContext.packageName))
                    startActivityForResult(intent, 2296)
                } catch (e: java.lang.Exception) {
                    val intent = Intent()
                    intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    startActivityForResult(intent, 2296)
                }
            } else {
                //below android 11
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(WRITE_EXTERNAL_STORAGE),
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
                )
            }
        }


//        if (ContextCompat.checkSelfPermission(
//                this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            )
//            != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(
//                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
//            )
//        } else {
//            readFile()
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, @Nullable data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data == null || resultCode != RESULT_OK || data.data == null)
            return
        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    readFile()
                    // perform action when allow permission success
                } else {
                    Toast.makeText(this, "Allow permission for storage access!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        } else if (requestCode == INTENT_REQUEST_PICK_FILE_CODE) {
            try {
                if (isGoogleDrive(data.data!!)) {
                    var file = getFileFromUri(this, data.data!!)
                    gotoViewPdf(file?.absolutePath!!)

                } else {
                    val path: String = RealPathUtil.getInstance().getRealPath(this, data.data)
                    gotoViewPdf(path)
                }

            } catch (e: Exception) {

            }

        }
    }

    @Throws(java.lang.Exception::class)
    fun getFileFromUri(context: Context, uri: Uri): File? {
        return if (isGoogleDrive(uri)) // check if file selected from google drive
        {
            saveFileIntoExternalStorageByUri(context, uri)
        } else  // do your other calculation for the other files and return that file
            null
    }


    fun isGoogleDrive(uri: Uri): Boolean {
        return "com.google.android.apps.docs.storage.legacy" == uri.authority
    }

    @Throws(java.lang.Exception::class)
    fun saveFileIntoExternalStorageByUri(context: Context, uri: Uri): File? {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val originalSize: Int? = inputStream?.available()
        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null
        val fileName = getFileName(context, uri)
        val file = makeEmptyFileIntoExternalStorageWithTitle(fileName)
        bis = BufferedInputStream(inputStream)
        bos = BufferedOutputStream(
            FileOutputStream(
                file, false
            )
        )
        val buf = ByteArray(originalSize!!)
        bis.read(buf)
        do {
            bos.write(buf)
        } while (bis.read(buf) !== -1)
        bos.flush()
        bos.close()
        bis.close()
        return file
    }

    fun getFileName(context: Context, uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            } finally {
                cursor?.close()
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result.substring(cut + 1)
            }
        }
        return result
    }


    fun makeEmptyFileIntoExternalStorageWithTitle(title: String?): File {
        val root = Environment.getExternalStorageDirectory().absolutePath
        return File(root, title)
    }

    fun gotoViewPdf(path: String) {
        var intent = Intent(this, PdfViewerActivity::class.java)
        intent.putExtra("url", path)
        startActivity(intent)
    }


    private fun checkPermission(): Boolean {
        return if (SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            val result =
                ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
            val result1 =
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
            result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults != null && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readFile()
            } else {

            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun readFile() {
        Thread {
            var listData = getExternalPDFFileList()
            application?.global?.listData?.postValue(listData)
        }.start()


    }

    fun composeEmail() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data =
                Uri.parse("mailto: elaineeyui@gmail.com") // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, "elaineeyui@gmail.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback about PDF Reader")
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun shareApp() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "PDF Reader")
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage =
                """${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Choose one"))
        } catch (e: Exception) {
            //e.toString();
        }
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

    override fun onNegativeButtonClicked() {

    }

    override fun onNeutralButtonClicked() {
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
//            finish()
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

//            finish()
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

        var editor = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE).edit()
        editor.putBoolean("isRating", true)
        editor.apply()
    }

    fun showInputSort() {
        var mSortModel = application?.global?.sortData?.value
        val view = layoutInflater.inflate(R.layout.dialog_sort, null)

        val rbName = view.findViewById(R.id.rbName) as RadioButton
        val rbSize = view.findViewById(R.id.rbSize) as RadioButton
        val rbDate = view.findViewById(R.id.rbDate) as RadioButton


        val rbInc = view.findViewById(R.id.rbInc) as RadioButton
        val rbDesc = view.findViewById(R.id.rbDesc) as RadioButton

        val lnName = view.findViewById(R.id.lnName) as FrameLayout
        val lnSize = view.findViewById(R.id.lnSize) as FrameLayout
        val lnDate = view.findViewById(R.id.lnDate) as FrameLayout

        val lnEsc = view.findViewById(R.id.lnEsc) as FrameLayout
        val lnDesc = view.findViewById(R.id.lnDesc) as FrameLayout

        val ok = view.findViewById<AppCompatButton>(R.id.ok)
        val cancel = view.findViewById<AppCompatButton>(R.id.cancel)

        lnName.setOnClickListener {
            rbName.isChecked = true
            rbDate.isChecked = false
            rbSize.isChecked = false
        }

        lnSize.setOnClickListener {
            rbSize.isChecked = true
            rbName.isChecked = false
            rbDate.isChecked = false
        }

        lnDate.setOnClickListener {
            rbDate.isChecked = true
            rbName.isChecked = false
            rbSize.isChecked = false
        }

        lnEsc.setOnClickListener {
            rbInc.isChecked = true
            rbDesc.isChecked = false
        }

        lnDesc.setOnClickListener {
            rbDesc.isChecked = true
            rbInc.isChecked = false
        }


        if (mSortModel != null) {
            if (mSortModel.type.equals("1")) {
                rbSize.isChecked = true
                rbName.isChecked = false
                rbDate.isChecked = false
            } else if (mSortModel.type.equals("2")) {
                rbDate.isChecked = true
                rbName.isChecked = false
                rbSize.isChecked = false
            } else {
                rbName.isChecked = true
                rbDate.isChecked = false
                rbSize.isChecked = false
            }
            if (mSortModel.order.equals("0")) {
                rbInc.isChecked = true
                rbDesc.isChecked = false
            } else {
                rbDesc.isChecked = true
                rbInc.isChecked = false

            }
        }
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()
        Objects.requireNonNull(dialog.window)
            ?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        ok.setOnClickListener { v: View? ->
            var type = "0"
            var order = "0"
            if (rbName.isChecked) {
                type = "0"
            }
            if (rbSize.isChecked) {
                type = "1"
            }
            if (rbDate.isChecked) {
                type = "2"
            }

            if (rbInc.isChecked) {
                order = "0"
            }
            if (rbDesc.isChecked) {
                order = "1"
            }
            var model = SortModel(type = type, order = order)
            saveSortStatus(model)
            application?.global?.sortData?.postValue(model)
            dialog.dismiss()
        }
        cancel.setOnClickListener { v: View? -> dialog.dismiss() }
    }

//    fun onConfirmDelete(path: String) {
//        val alertDialog: android.app.AlertDialog
//        val dialogBuilder = android.app.AlertDialog.Builder(context)
//        val inflater = this.layoutInflater
//        val dialogView: View = inflater.inflate(R.layout.dialog_delete_layout, null)
//        dialogBuilder.setView(dialogView)
//
//        val ok = dialogView.findViewById<AppCompatButton>(R.id.ok)
//        val cancel = dialogView.findViewById<AppCompatButton>(R.id.cancel)
//        alertDialog = dialogBuilder.create()
//        alertDialog.setCancelable(true)
//        Objects.requireNonNull(alertDialog.window)
//            ?.setBackgroundDrawableResource(android.R.color.transparent)
//        alertDialog.show()
//
//        ok.setOnClickListener { v: View? ->
//            deleteFile(path)
//            alertDialog.dismiss()
//        }
//        cancel.setOnClickListener { v: View? -> alertDialog.dismiss() }
//    }

    fun saveSortStatus(model: SortModel) {
        var editor = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE).edit()
        editor.putString("type", model.type)
        editor.putString("order", model.order)
        editor.apply()
    }

    fun getSortStatus(): SortModel {
        val prefs = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE)
        val type = prefs.getString("type", "2")
        val order = prefs.getString("order", "1")
        return SortModel(type = type, order = order)
    }

    fun showLanguageDialog(){
        val builderSingle = AlertDialog.Builder(this)
        builderSingle.setIcon(R.drawable.ic_icon_app)
        builderSingle.setTitle("Choose a language")
        builderSingle.setSingleChoiceItems(R.array.languages,0
        ) { p0, p1 -> }
        builderSingle.setNegativeButton("Cancel", DialogInterface.OnClickListener { dialogInterface, i ->

        })
        builderSingle.setPositiveButton("Ok", DialogInterface.OnClickListener { dialogInterface, i ->

        })
//        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
//            this, android.R.layout.simple_list_item_1
//        )
//
//        arrayAdapter.add("Customer 1 ")
//        arrayAdapter.add("Customer 2")
//
//        builderSingle.setAdapter(arrayAdapter, object : DialogInterface.OnClickListener() {
//            override fun onClick(dialog: DialogInterface?, which: Int) {
//                when (which) {
//                    0 -> {
//                    }
//                    1 -> {
//                    }
//                    2 -> {
//                    }
//                    else -> {
//                    }
//                }
//            }
//        })

        builderSingle.show()
    }
}