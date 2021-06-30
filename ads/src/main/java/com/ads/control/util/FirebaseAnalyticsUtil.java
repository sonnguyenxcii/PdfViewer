package com.ads.control.util;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.ads.control.Admod;
import com.google.android.gms.ads.AdValue;
import com.google.firebase.analytics.FirebaseAnalytics;

public class FirebaseAnalyticsUtil {
    public static void logPaidAdImpression(Context context, AdValue adValue, String adUnitId, String mediationAdapterClassName) {
        Log.d("FirebaseAnalyticsUtil", String.format(
                "Paid event of value %d microcents in currency %s of precision %s%n occurred for ad unit %s from ad network %s.",
                adValue.getValueMicros(),
                adValue.getCurrencyCode(),
                adValue.getPrecisionType(),
                adUnitId,
                mediationAdapterClassName));

        Bundle params = new Bundle(); // Log ad value in micros.
        params.putLong("valuemicros", adValue.getValueMicros());
        // These values below won’t be used in ROAS recipe.
        // But log for purposes of debugging and future reference.
        params.putString("currency", adValue.getCurrencyCode());
        params.putInt("precision", adValue.getPrecisionType());
        params.putString("adunitid", adUnitId);
        params.putString("network", mediationAdapterClassName);

        FirebaseAnalytics.getInstance(context).logEvent("paid_ad_impression", params);
    }
}
