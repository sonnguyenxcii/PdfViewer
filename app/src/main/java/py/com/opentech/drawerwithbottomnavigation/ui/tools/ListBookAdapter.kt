package py.com.opentech.drawerwithbottomnavigation.ui.tools

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_book_detail.*
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.BookModel
import py.com.opentech.drawerwithbottomnavigation.model.ResultModel


class ListBookAdapter(
    val context: Context,
    private val list: List<BookModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var pos = 0
    private val LIST_ITEM = 0
    private val GRID_ITEM = 1
    var isSwitchView = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val itemView: View
        itemView =
            LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_book, parent, false)

        return BookItemViewHolder(itemView)

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BookItemViewHolder) {
            val data: BookModel = list[position]
            holder.name.text = data.title
            holder.author.setText(data.author?.name)

            var imageUrl = ""
            try {
                data.images?.forEach {
                    if (it.size == "large") {
                        imageUrl = it.amazon_url!!
                    }
                }
                println("--------------------------" + imageUrl)
            } catch (e: Exception) {
                println("---e-----------------------" + imageUrl)

            }
            Glide.with(context).load(imageUrl)
                .placeholder(R.drawable.ic_ebook_placeholder)
                .into(holder.image)

            holder.itemView.setOnClickListener {
                var intent = Intent(context, BookDetailActivity::class.java)
                intent.putExtra("id", data.gutenberg_id)
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

class BookItemViewHolder(itemView: View) : RecyclerView.ViewHolder(
    itemView
) {
    val image = itemView.findViewById<AppCompatImageView>(R.id.image)
    val name = itemView.findViewById<AppCompatTextView>(R.id.name)
    val author = itemView.findViewById<AppCompatTextView>(R.id.author)

}
