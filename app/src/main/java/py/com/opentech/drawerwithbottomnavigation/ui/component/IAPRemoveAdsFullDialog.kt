//package com.prox.musicdownloader.utils.widgets.dialog
//
//import android.app.Dialog
//import android.content.Context
//import android.graphics.Paint
//import android.os.Bundle
//import android.util.DisplayMetrics
//import android.view.View
//import android.view.WindowManager
//import android.widget.TextView
//import androidx.core.os.bundleOf
//import com.google.firebase.analytics.ktx.analytics
//import com.google.firebase.ktx.Firebase
//import py.com.opentech.drawerwithbottomnavigation.R
//
//class IAPRemoveAdsFullDialog(private val mContext: Context) : Dialog(mContext, R.style.AppTheme) {
//
//    private var mOnPurchased: () -> Unit = {}
//    fun setCallback(callback: () -> Unit = {}) {
//        this.mOnPurchased = callback
//    }
//
//    private var mTryFreeCallback: () -> Unit = {onBackPressed()}
//    fun setTryFreeCallBack(callback: () -> Unit = {onBackPressed()}) {
//        this.mTryFreeCallback = callback
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.view_dialog_remove_ads_full)
//        window?.apply {
//            setWindowAnimations(R.style.IAPDialogAnimation)
//            setDimAmount(0.6f)
//        }
//        initView()
//    }
//
//    private fun initView() {
//        findViewById<View>(R.id.iv_close).setOnClickListener {
////            Firebase.analytics.logEvent(
////                FirebaseConst.Event.IAP_LAYOUT, bundleOf(
////                    FirebaseConst.Param.EVENT_TYPE to FirebaseConst.EventType.CLICKED_CLOSE
////                )
////            )
//
//            mTryFreeCallback()
//        }
//        findViewById<View>(R.id.tv_pucharse).setOnClickListener {
////            Firebase.analytics.logEvent(
////                FirebaseConst.Event.IAP_LAYOUT, bundleOf(
////                    FirebaseConst.Param.EVENT_TYPE to FirebaseConst.EventType.CLICKED_PAYMENT
////                )
////            )
//
//            mOnPurchased()
//        }
////        findViewById<View>(R.id.tv_try_it_free).setOnClickListener {
////            Firebase.analytics.logEvent(
////                FirebaseConst.Event.IAP_LAYOUT, bundleOf(
////                    FirebaseConst.Param.EVENT_TYPE to FirebaseConst.EventType.CLICKED_CLOSE
////                )
////            )
////
////            mTryFreeCallback()
////        }
//
//        val tvOldPrice = findViewById<TextView>(R.id.tv_old_price)
//        val tvPrice = findViewById<TextView>(R.id.tv_price)
//        if (Purchase.getInstance().discount == 1.0) {
//            tvOldPrice.visibility = View.GONE
//            findViewById<View>(R.id.view_split).visibility = View.GONE
//        } else {
//            tvOldPrice.visibility = View.VISIBLE
//            findViewById<View>(R.id.view_split).visibility = View.VISIBLE
//        }
////        tvOldPrice.text = Purchase.getInstance().oldPrice
////        tvPrice.text = Purchase.getInstance().price
////        tvOldPrice.paintFlags = tvOldPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
//    }
//
//
//}