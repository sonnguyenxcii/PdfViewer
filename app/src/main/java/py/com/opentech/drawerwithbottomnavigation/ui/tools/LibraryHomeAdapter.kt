package py.com.opentech.drawerwithbottomnavigation.ui.tools

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.ResultModel


class LibraryHomeAdapter(
    val context: Context,
    private val list: List<ResultModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var pos = 0
    private val LIST_ITEM = 0
    private val GRID_ITEM = 1
    var isSwitchView = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val itemView: View
        itemView =
            LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_list_cat_home, parent, false)

        return ItemViewHolder(itemView)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val data: ResultModel = list[position]
            holder.tittle.text = data.heading
            holder.subRecycleView.layoutManager =
                LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            var adapter = LibrarySubItemHomeAdapter(context, data.books!!)
            holder.subRecycleView.adapter = adapter

            holder.more.setOnClickListener {
                var intent = Intent(context, ListBookActivity::class.java)
                intent.putExtra("id", data.heading)
                context.startActivity(intent)
            }
        }
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
    val tittle = itemView.findViewById<AppCompatTextView>(R.id.tittle)
    val subRecycleView = itemView.findViewById<RecyclerView>(R.id.subRecycleView)
    val more = itemView.findViewById<AppCompatTextView>(R.id.more)
//    val date = itemView.findViewById<AppCompatTextView>(R.id.date)
//    val folder = itemView.findViewById<AppCompatTextView>(R.id.folder)
//    val bookmark = itemView.findViewById<View>(R.id.bookmark)
//    val bookmarkIcon = itemView.findViewById<AppCompatImageView>(R.id.bookmarkIcon)

}
