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
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.material.navigation.NavigationView
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import com.hosseiniseyro.apprating.AppRatingDialog
import com.hosseiniseyro.apprating.listener.RatingDialogListener
import com.infideap.drawerbehavior.AdvanceDrawerLayout
import kotlinx.android.synthetic.main.app_bar_default.*
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
    NavigationView.OnNavigationItemSelectedListener , RatingDialogListener {

    private var drawer: AdvanceDrawerLayout? = null
    private lateinit var navController: NavController
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    protected var application: PdfApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_layout)
        application = PdfApplication.create(this)

        this.application?.global?.isListMode?.postValue(true)

        setupNavController()
        Admod.getInstance().loadBanner(this, Constants.ADMOB_Banner)

        Admod.getInstance().initVideoAds(this,Constants.ADMOB_Reward)

        drawer = findViewById<View>(R.id.drawer_layout) as AdvanceDrawerLayout

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        drawer!!.setViewScale(Gravity.START, 0.9f)
        drawer!!.setRadius(Gravity.START, 0f)
        drawer!!.setViewElevation(Gravity.START, 20f)

        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(1).isEnabled = false

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            if (item.itemId == R.id.recent){
//                if (InternetConnection.checkConnection(this)){
                    Admod.getInstance().forceShowInterstitial(
                        this,
                        application?.mInterstitialAd,
                        object : AdCallback() {
                            override fun onAdClosed() {
                                navigateTo(item.itemId)
                            }
                        }
                    )
//                }else{
//                    navigateToBookmark()
//                }
            }else{
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
//            if (InternetConnection.checkConnection(this)){
                Admod.getInstance().forceShowInterstitial(
                    this,
                    application?.mInterstitialAd,
                    object : AdCallback() {
                        override fun onAdClosed() {
                            navigateToBookmark()
                        }
                    }
                )
//            }else{
//                navigateToBookmark()
//            }
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

    fun navigateToBookmark(){
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
                navigateToFile()
//                Toast.makeText(this, "file", Toast.LENGTH_SHORT).show()
//                drawer!!.openDrawer(GravityCompat.END)
                drawer!!.closeDrawer(GravityCompat.START)
                return false

            }
            R.id.nav_pdf_scan -> {
                if (!InternetConnection.checkConnection(this)){
                    navigateToScan()

                }else{
                    var isEarn = false
                    Admod.getInstance().loadVideoAds(this,object : RewardedAdCallback(){
                        override fun onUserEarnedReward(p0: RewardItem) {
                            isEarn = true
                        }

                        override fun onRewardedAdClosed() {
                            super.onRewardedAdClosed()
                            if (isEarn){
                                navigateToScan()
                            }
                        }

                        override fun onRewardedAdFailedToShow(p0: AdError?) {
                            super.onRewardedAdFailedToShow(p0)
                            navigateToScan()

                        }
                    })
                }


                drawer!!.closeDrawer(GravityCompat.START)
                return false
            }

            R.id.nav_pdf_merge -> {
                 if (!InternetConnection.checkConnection(this)){
                     navigateToMerge()

                }else{
                     var isEarn = false
                     Admod.getInstance().loadVideoAds(this,object : RewardedAdCallback(){
                         override fun onUserEarnedReward(p0: RewardItem) {
                             isEarn = true
                         }

                         override fun onRewardedAdClosed() {
                             super.onRewardedAdClosed()
                             if (isEarn){
                                 navigateToMerge()
                             }

                         }

                         override fun onRewardedAdFailedToShow(p0: AdError?) {
                             super.onRewardedAdFailedToShow(p0)
                             navigateToMerge()

                         }
                     })
                }


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

        val ROOT_DIR = Environment.getExternalStorageDirectory().absolutePath
        val ANDROID_DIR = File("$ROOT_DIR/Android")
        val DATA_DIR = File("$ROOT_DIR/data")
        File(ROOT_DIR).walk()
            // befor entering this dir check if
            .onEnter {
                !it.isHidden // it is not hidden
                        && it != ANDROID_DIR // it is not Android directory
                        && it != DATA_DIR // it is not data directory
                        && !File(it, ".nomedia").exists() //there is no .nomedia file inside
            }.filter { it.extension == "pdf" }
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
        intent.data = Uri.parse("mailto: chongdaquan@gmail.com") // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, "chongdaquan@gmail.com")
        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback about PDF Reader")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    fun shareApp(){
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "PDF Reader")
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage ="""${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}""".trimIndent()
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

    override fun onNegativeButtonClicked() {

    }

    override fun onNeutralButtonClicked() {
    }

    override fun onPositiveButtonClickedWithComment(rate: Int, comment: String) {
        if (rate>=4){
            askRatings()

        }else{
//            finish()
        }
    }

    override fun onPositiveButtonClickedWithoutComment(rate: Int) {
        if (rate>=4){
            askRatings()

        }else{
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