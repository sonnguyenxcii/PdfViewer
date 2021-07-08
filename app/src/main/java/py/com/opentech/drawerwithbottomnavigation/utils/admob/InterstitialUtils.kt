package py.com.opentech.drawerwithbottomnavigation.utils.admob

import android.content.Context
import android.util.Log
import com.ads.control.Admod
import com.google.android.gms.ads.InterstitialAd
import py.com.opentech.drawerwithbottomnavigation.utils.Constants

object InterstitialUtils {
    private var mInterClickOpenFile: InterstitialAd? = null

    fun initInterstitialStartup(context: Context) {
        if (mInterClickOpenFile == null) {
            Log.d("hoangpm", "init intersritial startup")
            mInterClickOpenFile = Admod.getInstance().getInterstitalAds(context, Constants.ADMOB_Iterstitial_Open_From_Other_App_208)
        }
    }

    fun getInterClickOpenFile() : InterstitialAd? {
        return mInterClickOpenFile
    }

}