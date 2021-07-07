package py.com.opentech.drawerwithbottomnavigation.ui.home

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ads.control.Admod
import com.ads.control.funtion.AdCallback
import com.google.android.material.bottomsheet.BottomSheetDialog
import io.realm.Realm
import io.realm.RealmResults
import org.greenrobot.eventbus.EventBus
import py.com.opentech.drawerwithbottomnavigation.BuildConfig
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.SearchActivity
import py.com.opentech.drawerwithbottomnavigation.model.FileChangeEvent
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import py.com.opentech.drawerwithbottomnavigation.model.SortModel
import py.com.opentech.drawerwithbottomnavigation.model.realm.BookmarkRealmObject
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfViewerActivity
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfViewerInAppAdsLoadingActivity
import py.com.opentech.drawerwithbottomnavigation.utils.CommonUtils
import py.com.opentech.drawerwithbottomnavigation.utils.Constants
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : Fragment(), RecycleViewOnClickListener {

    var listData: ArrayList<PdfModel> = ArrayList()
    lateinit var adapter: HomeAdapter
    protected var application: PdfApplication? = null

    var isListMode: Boolean = false
    var sort: SortModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycleView)
        val searchBox: View = root.findViewById(R.id.searchBox)

        Admod.getInstance().loadSmallNativeFragment(activity, Constants.ADMOB_Native_Home, root)

        recyclerView.layoutManager =
            if (isListMode) LinearLayoutManager(requireContext()) else GridLayoutManager(
                requireContext(),
                2
            )

        adapter = HomeAdapter(requireContext(), listData, this)
        recyclerView.adapter = adapter
        application = PdfApplication.create(activity)

        application?.global?.listData?.observe(viewLifecycleOwner, Observer { list ->
            if (list != null) {

                listData.clear()

                list.forEach {
                    var model = getBookmarkByPath(it.path!!)
                    it.isBookmark = !model.isNullOrEmpty()

                }

                listData.addAll(list)
                processData()
                adapter.notifyDataSetChanged()

                try {

                    val params = Bundle()
                    params.putString("total", "" + listData.size)
                    application?.firebaseAnalytics?.logEvent("Home_Layout_File_Loaded", params)
                } catch (e: Exception) {

                }
            }
        })

        application?.global?.isListMode?.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                isListMode = it
                recyclerView.layoutManager =
                    if (isListMode) LinearLayoutManager(requireContext()) else GridLayoutManager(
                        requireContext(),
                        2
                    )
                adapter.isSwitchView = isListMode
                adapter.notifyDataSetChanged()
            }
        })

        application?.global?.sortData?.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                sort = it
                processData()
                adapter.notifyDataSetChanged()

            }
        })

        searchBox.setOnClickListener {
            val params = Bundle()
            params.putString("button_click", "Button Search")
            application?.firebaseAnalytics?.logEvent("Home_Layout", params)

            if (application?.mInterstitialSearchAd == null) {
                application?.mInterstitialSearchAd = Admod.getInstance()
                    .getInterstitalAds(context, Constants.ADMOB_Interstitial_Search)
            }

            var intent = Intent(context, SearchActivity::class.java)
            startActivity(intent)
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().postSticky(FileChangeEvent())

    }

    override fun onItemClick(pos: Int) {

        val params = Bundle()
        params.putString("button_click", "Open File")
        application?.firebaseAnalytics?.logEvent("Home_Layout", params)

        try {
            onPrepareOpenAds(listData[pos].path!!)

        } catch (e: Exception) {

        }

    }

    @SuppressLint("RestrictedApi")
    override fun onMoreClick(pos: Int, view: View) {

        var model: PdfModel? = null

        try {
            model = listData[pos]

        } catch (e: Exception) {

        }

        if (model == null) {
            return
        }

        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_layout)

        val name = bottomSheetDialog.findViewById<AppCompatTextView>(R.id.name)
        val date = bottomSheetDialog.findViewById<AppCompatTextView>(R.id.date)
        val upload = bottomSheetDialog.findViewById<View>(R.id.upload)
        val printer = bottomSheetDialog.findViewById<View>(R.id.printer)
        val lnRename = bottomSheetDialog.findViewById<View>(R.id.lnRename)
        val lnBookmark = bottomSheetDialog.findViewById<View>(R.id.lnBookmark)
        val txtBookmark = bottomSheetDialog.findViewById<AppCompatTextView>(R.id.txtBookmark)
        val lnShare = bottomSheetDialog.findViewById<View>(R.id.lnShare)
        val lnMerge = bottomSheetDialog.findViewById<View>(R.id.lnMerge)
        val lnDelete = bottomSheetDialog.findViewById<View>(R.id.lnDelete)

        name?.text = model.name
        date?.text = model.date

        if (model.isBookmark!!) {
            txtBookmark?.setText("Remove from bookmark")
        } else {
            txtBookmark?.setText("Bookmark")
        }

        upload?.setOnClickListener {
            model.path?.let { it1 -> CommonUtils.onFileClick(requireContext(), it1) }
            bottomSheetDialog.dismiss()

        }
        printer?.setOnClickListener {
            try {
                model.path?.let { it1 -> CommonUtils.onActionPrint(requireContext(), it1) }
            } catch (e: Exception) {
                Toast.makeText(
                    requireContext(),
                    "Temporarily unable to print the file, the feature will be available soon",
                    Toast.LENGTH_SHORT
                ).show()
            }
            bottomSheetDialog.dismiss()

        }
        lnRename?.setOnClickListener {
            try {
                model.path?.let { it1 -> onConfirmRename(it1) }
            } catch (e: Exception) {

            }
            bottomSheetDialog.dismiss()

        }

        lnBookmark?.setOnClickListener {
            try {
                onBookmarkClick(pos)
//                model.path?.let { it1 -> addToBookmark(it1) }
            } catch (e: Exception) {

            }
            bottomSheetDialog.dismiss()

        }

        lnShare?.setOnClickListener {
            try {
                model.path?.let { it1 -> share(it1) }
            } catch (e: Exception) {

            }
            bottomSheetDialog.dismiss()

        }
        lnDelete?.setOnClickListener {
            try {
                model.path?.let { it1 -> onConfirmDelete(it1) }
            } catch (e: Exception) {

            }
            bottomSheetDialog.dismiss()

        }
        lnDelete?.setOnClickListener {
            try {
                model.path?.let { it1 -> onConfirmDelete(it1) }
            } catch (e: Exception) {

            }
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.show()
    }

    fun onPrepareOpenAds(path: String) {
//        Admod.getInstance().forceShowInterstitial(
//            context,
//            application?.mInterstitialClickOpenAd,
//            object : AdCallback() {
//                override fun onAdClosed() {
        gotoViewPdf(path)
//                }

//            }
//        )
    }

    override fun onBookmarkClick(pos: Int) {
        try {
            var data = listData[pos]
            var bookmarkStatus = data.isBookmark!!
            val params = Bundle()

            if (bookmarkStatus) {
                params.putString("bookmark_file", "0")

                deleteFromBookmark(data.path!!)
                Toast.makeText(context, "Removed from bookmark", Toast.LENGTH_SHORT).show()

            } else {
                params.putString("bookmark_file", "1")

                addToBookmark(data.path!!)
                Toast.makeText(context, "Added to bookmark", Toast.LENGTH_SHORT).show()

            }

            application?.firebaseAnalytics?.logEvent("Home_Layout", params)

            data.isBookmark = !bookmarkStatus
            adapter.notifyItemChanged(pos)
        } catch (e: Exception) {

        }

    }

    fun deleteFromBookmark(path: String) {
        var realm = Realm.getDefaultInstance()

        realm.executeTransaction { realm ->
            val result: RealmResults<BookmarkRealmObject> =
                realm.where(BookmarkRealmObject::class.java).equalTo("path", path).findAll()
            result.deleteAllFromRealm()
        }
    }

    private fun createShortcut(path: String) {
        val filename: String = path.substring(path.lastIndexOf("/") + 1)

        var shortcutManager: ShortcutManager? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager = context?.getSystemService(ShortcutManager::class.java)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (shortcutManager != null) {
                if (shortcutManager.isRequestPinShortcutSupported) {
                    val shortcut = ShortcutInfo.Builder(context, getString(R.string.app_name))
                        .setShortLabel(filename)
                        .setLongLabel(filename)
                        .setIcon(Icon.createWithResource(context, R.drawable.ic_pdf))
                        .setIntent(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(path)
                            )
                        )
                        .build()
                    shortcutManager.requestPinShortcut(shortcut, null)
                } else Toast.makeText(
                    context,
                    "Pinned shortcuts are not supported!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun share(path: String) {
        val intentShareFile = Intent(Intent.ACTION_SEND)
        val fileWithinMyDir = File(path)

        if (fileWithinMyDir.exists()) {
            intentShareFile.type = "application/pdf"
            val photoURI = context?.let {
                FileProvider.getUriForFile(
                    it, BuildConfig.APPLICATION_ID + ".provider",
                    fileWithinMyDir
                )
            }
            intentShareFile.putExtra(Intent.EXTRA_STREAM, photoURI)
            intentShareFile.putExtra(
                Intent.EXTRA_SUBJECT,
                "Sharing File..."
            )
            intentShareFile.putExtra(Intent.EXTRA_TEXT, "Sharing File...")
            startActivity(Intent.createChooser(intentShareFile, "Share File"))
        }
    }

    fun addToBookmark(path: String) {
        var model = getBookmarkByPath(path)
        if (model.isNullOrEmpty()) {
            saveBookmark(path)
        }
    }

    fun saveBookmark(path: String) {
        var realm = Realm.getDefaultInstance()
        var id = PdfApplication.bookmarkPrimaryKey.getAndIncrement();

        realm.executeTransactionAsync { realm ->
            val model: BookmarkRealmObject =
                realm?.createObject(BookmarkRealmObject::class.java, id)!!
            model.path = path
        }
    }


    fun getBookmarkByPath(path: String): List<BookmarkRealmObject?>? {
        var realm = Realm.getDefaultInstance()

        return realm.where(BookmarkRealmObject::class.java).equalTo("path", path).findAll()
    }


    fun deleteFile(path: String) {
        val fdelete = File(path)
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted :" + path)
            } else {
                System.out.println("file not Deleted :" + path)
            }
        }
        EventBus.getDefault().postSticky(FileChangeEvent())

    }


    fun gotoViewPdf(path: String) {

        if (application?.checkShowAdsOpen() == false) {
            var intent = Intent(context, PdfViewerActivity::class.java)
            intent.putExtra("url", path)
            startActivity(intent)
            return
        }
        var intent = Intent(context, PdfViewerInAppAdsLoadingActivity::class.java)
        intent.putExtra("url", path)
        startActivity(intent)
    }

    fun onConfirmDelete(path: String) {
        val alertDialog: AlertDialog
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = this.layoutInflater
        val dialogView: View = inflater.inflate(R.layout.dialog_delete_layout, null)
        dialogBuilder.setView(dialogView)

        val ok = dialogView.findViewById<AppCompatButton>(R.id.ok)
        val cancel = dialogView.findViewById<AppCompatButton>(R.id.cancel)
        alertDialog = dialogBuilder.create()
        alertDialog.setCancelable(true)
        Objects.requireNonNull(alertDialog.window)
            ?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()

        ok.setOnClickListener { v: View? ->
            deleteFile(path)
            alertDialog.dismiss()
        }
        cancel.setOnClickListener { v: View? -> alertDialog.dismiss() }
    }

    fun processData() {
        if (sort == null) {
            sort = SortModel(type = "0", order = "0")
        }

        if (sort!!.order.equals("0")) {
            if (sort!!.type.equals("0")) {
                listData.sortBy {
                    it.name
                }
            } else if (sort!!.type.equals("1")) {
                listData.sortBy {
                    it.size
                }
            } else {
                listData.sortBy {
                    it.lastModifier
                }
            }
        } else {
            if (sort!!.type.equals("0")) {
                listData.sortByDescending {
                    it.name
                }
            } else if (sort!!.type.equals("1")) {
                listData.sortByDescending {
                    it.size
                }
            } else {
                listData.sortByDescending {
                    it.lastModifier
                }
            }
        }
    }


    fun onConfirmRename(path: String) {
        val view = layoutInflater.inflate(R.layout.dialog_input_name, null)
        val categoryEditText = view.findViewById(R.id.categoryEditText) as EditText
        val currentFile = File(path)
        var fileName = currentFile.nameWithoutExtension
        categoryEditText.setText(fileName)
        val dialog: AlertDialog =
            AlertDialog.Builder(context)
                .setTitle("Rename file")
                .setMessage("Input name of file")
                .setView(view)
                .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                    val text = categoryEditText.text.toString()
                    renameFile(path, text)

                })
                .setNegativeButton("Cancel", null)
                .create()
        dialog.show()
    }


    fun renameFile(path: String, newName: String) {
        val currentFile = File(path)
        var oldName = currentFile.nameWithoutExtension
        val newFile = File(path.replace(oldName, newName).trim())
        println("-path--------------------" + path)
        println("-replace--------------------" + path.replace(oldName, newName))
        if (rename(currentFile, newFile)) {
            //Success
            Log.i("HomeFragment", "Success")
        } else {
            //Fail
            Log.i("HomeFragment", "Fail")
        }
        EventBus.getDefault().postSticky(FileChangeEvent())

    }

    private fun rename(from: File, to: File): Boolean {
        return from.parentFile.exists() && from.exists() && from.renameTo(to)
    }

}