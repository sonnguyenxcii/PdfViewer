package py.com.opentech.drawerwithbottomnavigation.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel

class HomeAdapter(
    val context: Context,
    private val list: List<PdfModel>,
    val clickListener: RecycleViewOnClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var pos = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(inflater, parent)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val movie: PdfModel = list[position]
            holder.bind(holder.itemView.context, movie)


//            holder.root?.setOnClickListener {
//                clickListener.onItemClick(holder.adapterPosition)
//
//            }
//
//            holder.sign?.setOnClickListener {
//                clickListener.onItemClick(position)
//            }


        }
    }


    override fun getItemCount(): Int = list.size

//    override fun getItemViewType(position: Int): Int {
//        if (position < itemCount - 1) {
//            return 1
//        }
//        return 2
//    }
}


class ItemViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.item_list_home, parent, false)) {
//    var username: RegularEditext? = null
//    var getOtp: View? = null
//    var hint: RegularTextView? = null
//    var delete_tit: RegularTextView? = null
//    var delete: View? = null
//    var edit: View? = null
//    var cstrEdit: View? = null
//    var okBtn: RegularTextView? = null
//    var cancelBtn: RegularTextView? = null
//    var edit_tit: RegularTextView? = null
//    var time: RegularTextView? = null
//    var sign: RegularTextView? = null
//    var qrCode: RegularTextView? = null
//
//    var root: SwipeRevealLayout? = null


    init {
//        getOtp = itemView.findViewById(R.id.getOtp)
//        username = itemView.findViewById(R.id.username)
//        delete = itemView.findViewById(R.id.delete)
//        root = itemView.findViewById(R.id.root)
//        delete_tit = itemView.findViewById(R.id.delete_tit)
//        edit = itemView.findViewById(R.id.edit)
//        cstrEdit = itemView.findViewById(R.id.cstr_edit)
//        edit_tit = itemView.findViewById(R.id.edit_tit)
//        hint = itemView.findViewById(R.id.hint)
//        okBtn = itemView.findViewById(R.id.ok_btn)
//        cancelBtn = itemView.findViewById(R.id.cancel_btn)
//        time = itemView.findViewById(R.id.time)
//        sign = itemView.findViewById(R.id.sign)
//        qrCode = itemView.findViewById(R.id.qr_code)
    }

    @SuppressLint("SetTextI18n")
    fun bind(context: Context, account: PdfModel) {

//        username?.setText(account.accountInfo.username)
//        hint?.text = account.accountInfo.displayName
////        getOtp?.text = context.getString(R.string.get_otp)
//        delete_tit?.text = context.getString(R.string.delete)
//        edit_tit?.text = context.getString(R.string.edit)
//        okBtn?.text = context.getString(R.string.save)
//        cancelBtn?.text = context.getString(R.string.edit_cancel)
//        time?.text = "Time"
//        sign?.text = "Sign"
//
//        getOtp?.visibility = View.VISIBLE
//        cstrEdit?.visibility = View.INVISIBLE

    }

}