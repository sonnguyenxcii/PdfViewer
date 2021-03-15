package py.com.opentech.drawerwithbottomnavigation.ui.home

import android.view.View

interface RecycleViewOnClickListener {
    fun onItemClick(pos :Int)
    fun onMoreClick(pos :Int,view : View)
    fun onBookmarkClick(pos :Int)

}