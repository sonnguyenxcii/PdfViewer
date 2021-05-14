package py.com.opentech.drawerwithbottomnavigation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.ads.control.Admod
import com.ads.control.funtion.AdCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import com.hosseiniseyro.apprating.AppRatingDialog
import com.hosseiniseyro.apprating.listener.RatingDialogListener
import com.infideap.drawerbehavior.AdvanceDrawerLayout
import kotlinx.android.synthetic.main.app_bar_default.*
import kotlinx.android.synthetic.main.include_preload_ads.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import py.com.opentech.drawerwithbottomnavigation.model.FileChangeEvent
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import py.com.opentech.drawerwithbottomnavigation.ui.fileexplorer.FileExplorerActivity
import py.com.opentech.drawerwithbottomnavigation.ui.merge.MergePdfActivity
import py.com.opentech.drawerwithbottomnavigation.ui.scan.ScanPdfActivity
import py.com.opentech.drawerwithbottomnavigation.utils.Constants
import py.com.opentech.drawerwithbottomnavigation.utils.InternetConnection
import py.com.opentech.drawerwithbottomnavigation.utils.Utils
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class HomeActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener, RatingDialogListener {

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

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.recent) {

                val params = Bundle()
                params.putString("button_click", "Recently")
                application?.firebaseAnalytics?.logEvent("Home_Layout", params)

                if (InternetConnection.checkConnection(this)) {
                    Admod.getInstance().forceShowInterstitial(
                        this,
                        application?.mInterstitialClickTabAd,
                        object : AdCallback() {
                            override fun onAdClosed() {
                                navigateTo(item.itemId)
                            }
                        }
                    )
                } else {
                    navigateTo(item.itemId)
                }
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
            if (InternetConnection.checkConnection(this)) {
                Admod.getInstance().forceShowInterstitial(
                    this,
                    application?.mInterstitialClickTabAd,
                    object : AdCallback() {
                        override fun onAdClosed() {
                            navigateToBookmark()
                        }
                    }
                )
            } else {
                navigateToBookmark()
            }
        }

        mode.setOnClickListener {
            var temp = this.application?.global?.isListMode?.value
            this.application?.global?.isListMode?.postValue(!temp!!)
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

        var intent = Intent(this, ScanPdfActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMerge() {

        var intent = Intent(this, MergePdfActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToFile() {

        var intent = Intent(this, FileExplorerActivity::class.java)
        startActivity(intent)
    }

    private fun getExternalPDFFileList(): ArrayList<PdfModel> {
        val uriList: ArrayList<PdfModel> = ArrayList()

        try {
            val ROOT_DIR = Environment.getExternalStorageDirectory().absolutePath
//            val ANDROID_DIR = File("$ROOT_DIR/Android")
//            val DATA_DIR = File("$ROOT_DIR/data")
            File(ROOT_DIR).walk()
                // befor entering this dir check if
//            .onEnter {
//                !it.isHidden // it is not hidden
//                        && it != ANDROID_DIR // it is not Android directory
//                        && it != DATA_DIR // it is not data directory
//                        && !File(it, ".nomedia").exists() //there is no .nomedia file inside
//            }
                .filter { it.extension == "pdf" }
                .toList().forEach {
                    val lastModDate = Date(it.lastModified())
                    uriList.add(
                        PdfModel(
                            it.name, it.absolutePath, it.length(), Utils.formatDate(
                                lastModDate
                            )
                        )
                    )
                }
        } catch (e: Exception) {

        }
        return uriList
    }

    fun requestRead() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            readFile()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data =
            Uri.parse("mailto: chongdaquan@gmail.com") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, "chongdaquan@gmail.com")
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback about PDF Reader")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
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

    override fun onNegativeButtonClicked() {

    }

    override fun onNeutralButtonClicked() {
    }

    override fun onPositiveButtonClickedWithComment(rate: Int, comment: String) {
        if (rate >= 4) {
            askRatings()

        } else {
//            finish()
        }
    }

    override fun onPositiveButtonClickedWithoutComment(rate: Int) {
        if (rate >= 4) {
            askRatings()

        } else {
//            finish()
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
//                    finish()
                }
            } else {
//                finish()
                // There was some problem, continue regardless of the result.
            }
        }
    }
}