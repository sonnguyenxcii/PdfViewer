package py.com.opentech.drawerwithbottomnavigation.utils.admob

import android.content.Context
import com.ads.control.Admod
import com.google.android.gms.ads.InterstitialAd
import py.com.opentech.drawerwithbottomnavigation.utils.Constants

object InterstitialUtils {
    private var mInterClickOpenFile: InterstitialAd? = null

    fun initInterstitialStartup(context: Context) {
        if (mInterClickOpenFile == null) {
            mInterClickOpenFile = Admod.getInstance().getInterstitalAds(context, Constants.ADMOB_Iterstitial_Open_From_Other_App_Test)
        }
    }

    fun getInterClickOpenFile() : InterstitialAd? {
        return mInterClickOpenFile
    }

}