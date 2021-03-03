package py.com.opentech.drawerwithbottomnavigation.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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
}