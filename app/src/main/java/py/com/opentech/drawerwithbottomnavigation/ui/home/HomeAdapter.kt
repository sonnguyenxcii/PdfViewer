package py.com.opentech.drawerwithbottomnavigation.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel


class HomeAdapter(
    val context: Context,
    private val list: List<PdfModel>,
    val clickListener: RecycleViewOnClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var pos = 0
    private val LIST_ITEM = 0
    private val GRID_ITEM = 1
    var isSwitchView = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val itemView: View
        if (viewType === LIST_ITEM) {
            itemView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_home, null)
        } else {
            itemView =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_home_grid, null)
        }
        return ItemViewHolder(itemView)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val data: PdfModel = list[position]
            holder.name?.setText(data.name)
            holder.size?.setText("" + data.size)
//            holder.bind(holder.itemView.context, movie)


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

    fun toggleItemViewType(): Boolean {
        isSwitchView = !isSwitchView
        return isSwitchView
    }

    override fun getItemCount(): Int = list.size

    override fun getItemViewType(position: Int): Int {
        if (isSwitchView) {
            return LIST_ITEM;
        }else{
            return GRID_ITEM;
        }
    }
}

class ItemViewHolder(itemView: View?) : RecyclerView.ViewHolder(
    itemView!!
) {
    @BindView(R.id.name)
    var name: TextView? = null

    @BindView(R.id.size)
    var size: TextView? = null


    init {
        ButterKnife.bind(this, itemView!!)
    }
}
