package py.com.opentech.drawerwithbottomnavigation

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import com.ads.control.AppPurchase
import com.ads.control.funtion.PurchaseListioner
import kotlinx.android.synthetic.main.activity_premium.*
import py.com.opentech.drawerwithbottomnavigation.utils.ToastUtils


class PremiumActivity : AppCompatActivity() {

    lateinit var currentPrice : AppCompatTextView
    lateinit var oldPrice : AppCompatTextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)
        currentPrice = findViewById(R.id.currentPrice);
        oldPrice = findViewById(R.id.oldPrice);
        currentPrice.setText(AppPurchase.getInstance().getPrice(PdfApplication.REMOVE_ADS))
        oldPrice.setText(AppPurchase.getInstance().getPrice(PdfApplication.REMOVE_ADS))
        AppPurchase.getInstance().consumePurchase(PdfApplication.REMOVE_ADS)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Premium"

        purchase.setOnClickListener {
            AppPurchase.getInstance()
                .consumePurchase(PdfApplication.REMOVE_ADS)
            AppPurchase.getInstance()
                .purchase(this, PdfApplication.REMOVE_ADS)
        }

        AppPurchase.getInstance().setPurchaseListioner(object : PurchaseListioner {

            override fun onProductPurchased(p0: String?, p1: String?) {
                PdfApplication.create(this@PremiumActivity).mIsPurchased.postValue(true)
                finish()
            }

            override fun displayErrorMessage(p0: String?) {
                PdfApplication.create(this@PremiumActivity).mIsPurchased.postValue(false)
                ToastUtils.showMessageShort(this@PremiumActivity, p0)
                Log.e("PurchaseListioner", "displayErrorMessage:$p0")
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> {

            }
        }
        return super.onOptionsItemSelected(item)
    }
}