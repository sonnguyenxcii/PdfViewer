package py.com.opentech.drawerwithbottomnavigation.ui.tools

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.api.ApiService
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import py.com.opentech.drawerwithbottomnavigation.model.ResultModel
import py.com.opentech.drawerwithbottomnavigation.ui.home.HomeAdapter

class ToolsFragment : Fragment() {
    protected var compositeDisposable = CompositeDisposable()

    private lateinit var toolsViewModel: ToolsViewModel
    lateinit var adapter: LibraryHomeAdapter
    var listData: ArrayList<ResultModel> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        toolsViewModel =
            ViewModelProviders.of(this).get(ToolsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_tools, container, false)

        val recyclerView: RecyclerView = root.findViewById(R.id.recycleView)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        getListCat()
        adapter = LibraryHomeAdapter(requireContext(), listData)
        recyclerView.adapter = adapter
        toolsViewModel.text.observe(viewLifecycleOwner, Observer {
        })
        return root
    }

    fun getListCat() {
        var application = PdfApplication.create(context)
        val peopleService: ApiService = application.jsonService
        val disposable: Disposable = peopleService.listCat
            .subscribeOn(application.subscribeScheduler())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ responseBody ->
                if (responseBody.data != null) {

                }
            }) { throwable ->

            }

        compositeDisposable.add(disposable)
    }
}