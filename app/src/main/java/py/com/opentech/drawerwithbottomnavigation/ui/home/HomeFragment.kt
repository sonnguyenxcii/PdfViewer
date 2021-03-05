package py.com.opentech.drawerwithbottomnavigation.ui.home

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfViewerActivity
import java.io.File


class HomeFragment : Fragment(), RecycleViewOnClickListener {

    var listData: ArrayList<PdfModel> = ArrayList()
    lateinit var adapter: HomeAdapter
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = HomeAdapter(requireContext(), listData, this)
        recyclerView.adapter = adapter

        requestRead()

        return root
    }

//    fun readFile() {
//        listData.addAll(getExternalPDFFileList())
//        adapter.notifyDataSetChanged()
//        // do something
//    }

    fun requestRead() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            readFile()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readFile()
            } else {
                // Permission Denied
//                Toast.makeText(this@ToolbarActivity, "Permission Denied", Toast.LENGTH_SHORT).show()
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onItemClick(pos: Int) {
        gotoViewPdf(listData[pos].path!!)
    }

    override fun onMoreClick(pos: Int) {

    }

    fun gotoViewPdf(path: String) {
        var intent = Intent(context, PdfViewerActivity::class.java)
        intent.putExtra("url", path)
        startActivity(intent)
    }

}