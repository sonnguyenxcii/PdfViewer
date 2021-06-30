package py.com.opentech.drawerwithbottomnavigation.ui.component;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.ads.control.AppPurchase;
import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

import py.com.opentech.drawerwithbottomnavigation.R;

public class ExitDialog extends Dialog
{
    UnifiedNativeAd ad;
    Activity activity;
    public ExitDialog(Activity activity, UnifiedNativeAd ad)
    {
        super(activity);
        this.activity = activity;
        this.ad = ad;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.exit_dialog);
        View yes =findViewById(R.id.btn_yes);
        View no =findViewById(R.id.btn_no);
        yes.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                activity.finish();
            }
        });
        no.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
            }
        });
        TemplateView ad = findViewById(R.id.ad_template);
        if(this.ad == null || AppPurchase.getInstance().isPurchased(activity))
        {
            ad.setVisibility(View.GONE);
        }
        else
        {
            ad.setVisibility(View.VISIBLE);
            ad.setNativeAd(this.ad);
        }
    }
}
