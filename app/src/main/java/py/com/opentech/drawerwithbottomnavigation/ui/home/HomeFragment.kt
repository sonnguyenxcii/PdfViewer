package py.com.opentech.drawerwithbottomnavigation.ui.home

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import java.io.File


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

//        getList()
        val recyclerView: RecyclerView = root.findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        adapter = HomeAdapter(requireContext(), listData, this)
        recyclerView.adapter = adapter

        Search_Dir(Environment.getDataDirectory());
        Search_Dir(Environment.getRootDirectory());
        Search_Dir(Environment.getExternalStorageDirectory());

        return root
    }

    override fun onItemClick(pos: Int) {

    }

    override fun onMoreClick(pos: Int) {

    }

    private fun getList() {
//        val list = ArrayList<PdfModel>()
        for (i in 0 until 10) {
            val model = PdfModel()
            model.name = "Name "+ i
            model.size = 56
            listData.add(model)
        }
//        return list
    }

    fun Search_Dir(dir: File) {
        val pdfPattern = ".pdf"
        val FileList: Array<File> = dir.listFiles()
        if (FileList != null) {
            for (i in FileList.indices) {
                if (FileList[i].isDirectory()) {
                    Search_Dir(FileList[i])
                } else {
                    if (FileList[i].getName().endsWith(pdfPattern)) {
                        //here you have that file.

                    }
                }
            }
        }
    }
//    private fun getExternalPDFFileList(): ArrayList<PdfModel>? {
//        val cr: ContentResolver = getContentResolver()
//        val uri: Uri = MediaStore.Files.getContentUri("external")
//        val projection =
//            arrayOf(MediaStore.Files.FileColumns._ID, MediaStore.Files.FileColumns.DISPLAY_NAME)
//        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
//                + MediaStore.Files.FileColumns.MEDIA_TYPE)
//        val selectionArgs: Array<String>? = null
//        val selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
//        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
//        val selectionArgsPdf = arrayOf(mimeType)
//        val cursor: Cursor = cr.query(uri, projection, selectionMimeType, selectionArgsPdf, null)!!
//        val uriList: ArrayList<PdfModel> = ArrayList()
//        cursor.moveToFirst()
//        while (!cursor.isAfterLast()) {
//            val columnIndex: Int = cursor.getColumnIndex(projection[0])
//            val fileId: Long = cursor.getLong(columnIndex)
//            val fileUri: Uri = Uri.parse(uri.toString().toString() + "/" + fileId)
//            val displayName: String = cursor.getString(cursor.getColumnIndex(projection[1]))
//            uriList.add(PdfModel(displayName, fileUri.toString()))
//            cursor.moveToNext()
//        }
//        cursor.close()
//        return uriList
//    }
}