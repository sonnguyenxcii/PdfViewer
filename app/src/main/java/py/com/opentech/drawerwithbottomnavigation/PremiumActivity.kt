package py.com.opentech.drawerwithbottomnavigation

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.ads.control.Purchase
import com.ads.control.funtion.PurchaseListioner
import kotlinx.android.synthetic.main.activity_premium.*
import py.com.opentech.drawerwithbottomnavigation.utils.ToastUtils


class PremiumActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_premium)
        Purchase.getInstance().consumePurchase(PdfApplication.PRODUCT_ID)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Premium"

        purchase.setOnClickListener {
            Purchase.getInstance()
                .consumePurchase(PdfApplication.PRODUCT_ID)
            Purchase.getInstance()
                .purchase(this, PdfApplication.PRODUCT_ID)
        }

        Purchase.getInstance().setPurchaseListioner(object : PurchaseListioner {

            fun displayErrorMessage(errorMsg: String) {
//                PdfApplication.create(this@PremiumActivity).mIsPurchased.postValue(false)
                ToastUtils.showMessageShort(this@PremiumActivity, errorMsg)
                Log.e("PurchaseListioner", "displayErrorMessage:$errorMsg")
            }

            override fun onProductPurchased(productId: String?) {
                Log.e("PurchaseListioner", "onProductPurchased:$productId")
                ToastUtils.showMessageShort(this@PremiumActivity, "Purchase Success")

//                PdfApplication.create(this@PremiumActivity).mIsPurchased.postValue(true)
                finish()
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