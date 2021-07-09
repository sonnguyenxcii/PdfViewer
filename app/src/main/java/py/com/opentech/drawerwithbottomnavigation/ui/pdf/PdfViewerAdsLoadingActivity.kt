package py.com.opentech.drawerwithbottomnavigation.ui.pdf

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ads.control.Admod
import com.ads.control.funtion.AdCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.utils.Constants
import py.com.opentech.drawerwithbottomnavigation.utils.admob.InterstitialUtils
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class PdfViewerAdsLoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preload_ads)
        InterstitialUtils.initInterstitialStartup(this)
        loadAds()
    }

    fun loadAds() {
        val prefs = getSharedPreferences(Constants.MY_PREFS_NAME, MODE_PRIVATE)
        val time = prefs.getLong("openFromOtherAppTimeOut", 5000)
        Admod.getInstance().loadSplashInterstitalAds(this,
            Constants.ADMOB_Iterstitial_Open_From_Other_App,
            time,
            object : AdCallback() {

                override fun onAdClosed() {
                    gotoRead()
                }

                override fun onAdFailedToLoad(i: Int) {
                    gotoRead()
                }
            })
//        MobileAds.initialize(this, new OnInitializationCompleteListener() {
//            @Override
//            public void onInitializationComplete(InitializationStatus initializationStatus) {
//            }
//        });
//        AdRequest adRequest = new AdRequest.Builder().build();
//
//        InterstitialAd.load(this, Constants.ADMOB_Iterstitial_Open_From_Other_App, adRequest, new InterstitialAdLoadCallback() {
//            @Override
//            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
//                // The mInterstitialAd reference will be null until
//                // an ad is loaded.
//                mInterstitialAd = interstitialAd;
////                Log.i(TAG, "onAdLoaded");
//                if (mInterstitialAd != null) {
//                    mInterstitialAd.show(PdfViewerAdsLoadingActivity.this);
//
//                    mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback(){
//                        @Override
//                        public void onAdDismissedFullScreenContent() {
//                            // Called when fullscreen content is dismissed.
//                            Log.d("TAG", "The ad was dismissed.");
//                            gotoRead();
//                        }
//
//                        @Override
//                        public void onAdFailedToShowFullScreenContent(AdError adError) {
//                            // Called when fullscreen content failed to show.
//                            Log.d("TAG", "The ad failed to show.");
//                            gotoRead();
//                        }
//
//                        @Override
//                        public void onAdShowedFullScreenContent() {
//                            // Called when fullscreen content is shown.
//                            // Make sure to set your reference to null so you don't
//                            // show it a second time.
//                            mInterstitialAd = null;
//                            Log.d("TAG", "The ad was shown.");
//                        }
//                    });
//                } else {
//                    Log.d("TAG", "The interstitial ad wasn't ready yet.");
//                }
//
//
//            }
//
//            @Override
//            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
//                // Handle the error
//                mInterstitialAd = null;
//                gotoRead();
//            }
//        });
    }

    fun gotoRead() {
        val file_uri = intent.data

        if (file_uri != null) {
            val url = getFilePathForN(file_uri, this)
            val intent = Intent(this, PdfViewerActivity::class.java)
            intent.putExtra("url", url)
            intent.putExtra("other_app", true)
            startActivity(intent)
            finish()
        }

    }

    @SuppressLint("Recycle")
    private fun getFilePathForN(uri: Uri, context: Context): String? {

        try {
            val returnCursor: Cursor = context.contentResolver.query(uri, null, null, null, null)!!
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
            return file.path
        } catch (e: Exception) {

            e.message?.let { Log.e("Exception", it) }
        }
        return uri.path
    }
}