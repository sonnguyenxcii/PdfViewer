package py.com.opentech.drawerwithbottomnavigation.ui.merge

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import py.com.opentech.drawerwithbottomnavigation.ui.home.RecycleViewOnClickListener
import py.com.opentech.drawerwithbottomnavigation.utils.Utils


class MultiFileSelectAdapter(
    val context: Context,
    private val list: List<PdfModel>, val clickListener: RecycleViewOnClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var pos = 0
    private val LIST_ITEM = 0
    private val GRID_ITEM = 1
    var isSwitchView = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val itemView: View
//        if (viewType == LIST_ITEM) {

            itemView =
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_file_select, parent, false)
//        } else {
//            itemView =
//                LayoutInflater.from(parent.getContext())
//                    .inflate(R.layout.item_list_home_grid, parent, false)
//        }
        return ItemViewHolder(itemView)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val data: PdfModel = list[position]
            holder.name.text = data.name
            holder.size.text = Utils.convertToStringRepresentation(data.size!!)
            holder.date.text = data.date

            holder.itemView.setOnClickListener {
                clickListener.onItemClick(holder.adapterPosition)
//                data.isCheck = !data.isCheck!!
//                notifyItemChanged(holder.adapterPosition)
            }

            holder.check.isChecked = data.isCheck!!

        }
    }

    fun toggleItemViewType(): Boolean {
        isSwitchView = !isSwitchView
        return isSwitchView
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        if (isSwitchView) {
            return LIST_ITEM
        } else {
            return GRID_ITEM
        }
    }
}

class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(
    itemView
) {
    val name = itemView.findViewById<AppCompatTextView>(R.id.name)
    val size = itemView.findViewById<AppCompatTextView>(R.id.size)
    val check = itemView.findViewById<AppCompatCheckBox>(R.id.check)
    val date = itemView.findViewById<AppCompatTextView>(R.id.date)

}
