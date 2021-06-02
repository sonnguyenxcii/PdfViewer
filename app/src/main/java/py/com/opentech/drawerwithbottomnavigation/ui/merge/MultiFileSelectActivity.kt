package py.com.opentech.drawerwithbottomnavigation.ui.merge

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ads.control.Admod
import com.ads.control.funtion.AdCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.tom_roush.pdfbox.io.MemoryUsageSetting
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.include_preload_ads.*
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import py.com.opentech.drawerwithbottomnavigation.ui.home.RecycleViewOnClickListener
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfViewerActivity
import py.com.opentech.drawerwithbottomnavigation.utils.Constants
import py.com.opentech.drawerwithbottomnavigation.utils.InternetConnection
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.String


class MultiFileSelectActivity : AppCompatActivity(), RecycleViewOnClickListener {
    var listData: ArrayList<PdfModel> = ArrayList()
    lateinit var adapter: MultiFileSelectAdapter
    protected var application: PdfApplication? = null
    var hideIcon: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_file)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Select the file"

        adRequest = AdRequest.Builder().build()

        val recyclerView: RecyclerView = findViewById(R.id.recycleView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MultiFileSelectAdapter(this, listData, this)
        recyclerView.adapter = adapter
        application = PdfApplication.create(this)

        application?.global?.listData?.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                listData.clear()
                listData.addAll(it)
                adapter.notifyDataSetChanged()
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.done -> {
                onDone()
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onDone() {
        val params = Bundle()
        params.putString("button_click", "Create/Merge")
        application?.firebaseAnalytics?.logEvent("Merge_PDF_Layout", params)

        showInputName()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_select_multi_file, menu)
        menu.findItem(R.id.done).isVisible = !hideIcon
        return true
    }

    override fun onItemClick(pos: Int) {
        var data = listData[pos]
        data.isCheck = !data.isCheck!!
        adapter.notifyItemChanged(pos)
        caculateSelectCount()

    }

    override fun onMoreClick(pos: Int, view: View) {

    }

    override fun onBookmarkClick(pos: Int) {

    }

    fun caculateSelectCount() {
        var count = 0
        listData.forEach {
            if (it.isCheck!!) {
                count++
            }
        }
        hideIcon = count < 2

        invalidateOptionsMenu()

    }

    var fileName = ""

    fun showInputName() {

        val view = layoutInflater.inflate(R.layout.dialog_input_name, null)
        val categoryEditText = view.findViewById(R.id.categoryEditText) as EditText
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Create PDF")
            .setMessage("Input name of file")
            .setView(view)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                val text = categoryEditText.text.toString()
                val params = Bundle()
                params.putString("button_click", "OK")
                application?.firebaseAnalytics?.logEvent("Merge_PDF_Layout", params)

                if (InternetConnection.checkConnection(this)) {
                    processToMerge(text)
//                    Admod.getInstance().forceShowInterstitial(
//                        this,
//                        application?.mInterstitialClickTabAd,
//                        object : AdCallback() {
//                            override fun onAdClosed() {
//                                doMergeTask(text)
//                            }
//                        }
//                    )
                } else {
                    doMergeTask(text)
                }


            })
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    fun doMergeTask(text: kotlin.String) {

        if (!TextUtils.isEmpty(text)) {
            fileName = text
            downloadAndCombinePDFs()
        }
    }

    @Throws(IOException::class)
    private fun downloadAndCombinePDFs(): File {
        val dialog: android.app.AlertDialog? = SpotsDialog.Builder().setContext(this).build()
        dialog?.show()

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
            "$fileName.pdf"
        )

        Thread {
            val ut = PDFMergerUtility()
            listData.forEach {
                if (it.isCheck!!) {
                    var fileTemp = File(it.path)
                    ut.addSource(fileTemp)
                }
            }

            if (file.exists())
                file.delete()

            val fileOutputStream = FileOutputStream(file)
            try {
                ut.destinationStream = fileOutputStream
                ut.mergeDocuments(true)
//                ut.mergeDocuments(MemoryUsageSetting.setupTempFileOnly())
            } catch (e: Exception) {
                e.printStackTrace()
                e.message?.let { showAlertDialog(it) }
            } finally {
                fileOutputStream.close()
                dialog?.dismiss()

            }

            gotoViewPdf(file.absolutePath)
        }.start()


        println("---------------" + file.absolutePath)
        return file
    }

    fun gotoViewPdf(path: kotlin.String) {
        var intent = Intent(this, PdfViewerActivity::class.java)
        intent.putExtra("url", path)
        startActivity(intent)
        finish()
    }

    fun showAlertDialog(msg: kotlin.String) {
        if (isFinishing) return
        try {
            val dialogBuilder = android.app.AlertDialog.Builder(this)
            dialogBuilder.setTitle(null)
            dialogBuilder.setIcon(R.mipmap.ic_launcher)
            dialogBuilder.setMessage(msg)
            dialogBuilder.setPositiveButton(
                "OK"
            ) { dialog: DialogInterface?, which: Int -> }
            dialogBuilder.setCancelable(false)
            dialogBuilder.show()
        } catch (e: java.lang.Exception) {
//            Debug.e("---  Exception BaseActivity " + e.message)
        }
    }

    private var adRequest: AdRequest? = null
    private var mInterstitialMergeAd: InterstitialAd? = null

    fun processToMerge(text: kotlin.String) {

        if (!InternetConnection.checkConnection(this)) {
            doMergeTask(text)

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
                                    doMergeTask(text)

                                }

                                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                    // Called when fullscreen content failed to show.
                                    println("----onAdFailedToShowFullScreenContent---------------")
                                    preloadAdsLayout.visibility = View.GONE
                                    doMergeTask(text)

                                }

                                override fun onAdShowedFullScreenContent() {
                                    preloadAdsLayout.visibility = View.GONE
                                    mInterstitialMergeAd = null

                                }
                            }

                        mInterstitialMergeAd!!.show(this@MultiFileSelectActivity)

                    }

                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        mInterstitialMergeAd = null
                        preloadAdsLayout.visibility = View.GONE
                        doMergeTask(text)

                    }
                })
        }
    }
}