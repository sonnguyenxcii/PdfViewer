package py.com.opentech.drawerwithbottomnavigation.ui.pdf;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.ads.control.Admod;
import com.ads.control.funtion.AdCallback;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import py.com.opentech.drawerwithbottomnavigation.PdfApplication;
import py.com.opentech.drawerwithbottomnavigation.R;
import py.com.opentech.drawerwithbottomnavigation.utils.Constants;

public class PdfViewerInAppAdsLoadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preload_ads);
        Admod.getInstance().forceShowInterstitial(this, PdfApplication.create(this).mInterstitialClickOpenAd,new AdCallback(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                gotoRead();
            }
        });
    }

    void gotoRead() {
        Intent intent = new Intent(this, PdfViewerActivity.class);
        intent.putExtra("url", getPath());
        startActivity(intent);
        finish();
    }

    private String getPath() {
        return getIntent().getStringExtra("url");
    }
}
