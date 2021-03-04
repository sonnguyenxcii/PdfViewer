package py.com.opentech.drawerwithbottomnavigation.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel

class HomeFragment : Fragment(), RecycleViewOnClickListener {

    //    private lateinit var homeViewModel: HomeViewModel
    var listData: ArrayList<PdfModel> = ArrayList()
    lateinit var adapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = HomeAdapter(context!!, listData, this)
        recyclerView.adapter = adapter
        return root
    }

    override fun onItemClick(pos: Int) {

    }

    private fun getList(): List<*>? {
        val list = ArrayList<PdfModel>()
        for (i in 0 until 10) {
            val model = PdfModel()
            model.name = ""
            model.setImagePath(imageUrl.get(i))
            list.add(model)
        }
        return list
    }
}