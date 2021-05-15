package py.com.opentech.drawerwithbottomnavigation.ui.bookmark

import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ads.control.Admod
import com.ads.control.funtion.AdCallback
import io.realm.Realm
import io.realm.RealmResults
import py.com.opentech.drawerwithbottomnavigation.BuildConfig
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import py.com.opentech.drawerwithbottomnavigation.model.SortModel
import py.com.opentech.drawerwithbottomnavigation.model.realm.BookmarkRealmObject
import py.com.opentech.drawerwithbottomnavigation.ui.home.HomeAdapter
import py.com.opentech.drawerwithbottomnavigation.ui.home.RecycleViewOnClickListener
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfViewerActivity
import py.com.opentech.drawerwithbottomnavigation.utils.Constants
import java.io.File

class BookmarkFragment : Fragment(), RecycleViewOnClickListener {

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

        val root = inflater.inflate(R.layout.fragment_bookmark, container, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycleView)
        recyclerView.layoutManager = if (isListMode) LinearLayoutManager(requireContext()) else GridLayoutManager(
            requireContext(),
            2
        )
        Admod.getInstance().loadSmallNativeFragment(activity, Constants.ADMOB_Native_Bookmark, root)

        adapter = HomeAdapter(requireContext(), listData, this)
        recyclerView.adapter = adapter
        application = PdfApplication.create(activity)

        application?.global?.isListMode?.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                isListMode = it
                recyclerView.layoutManager = if (isListMode) LinearLayoutManager(requireContext()) else GridLayoutManager(
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
        return root
    }

    override fun onResume() {
        super.onResume()
        listData.clear()
        var models = getBookmarkByPath()

        models?.forEach { bm ->
            application?.global?.listData?.value?.forEach { data ->
              if (bm?.path?.equals(data.path)!!){
                  data.isBookmark = true
                  listData.add(data)
                  processData()
              }
            }
        }
        adapter.notifyDataSetChanged()
    }

    fun getBookmarkByPath(): List<BookmarkRealmObject?>? {
        var realm = Realm.getDefaultInstance()

        return realm.where(BookmarkRealmObject::class.java).findAll()
    }



    override fun onItemClick(pos: Int) {

        val params = Bundle()
        params.putString("button_click", "Open File")
        application?.firebaseAnalytics?.logEvent("Bookmark_Layout", params)

//        Admod.getInstance().forceShowInterstitial(
//            context,
//            application?.mInterstitialAd,
//            object : AdCallback() {
//                override fun onAdClosed() {
//                    gotoViewPdf(listData[pos].path!!)
//                }
//            }
//        )
        onPrepareOpenAds(listData[pos].path!!)
    }

    override fun onMoreClick(pos: Int, view: View) {
        //Creating the instance of PopupMenu
        //Creating the instance of PopupMenu
        val popup = PopupMenu(requireContext(), view)
        //Inflating the Popup using xml file
        //Inflating the Popup using xml file
        popup.menuInflater
            .inflate(R.menu.poupup_bookmark_menu, popup.getMenu())

        //registering popup with OnMenuItemClickListener

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {

                if (item?.itemId == R.id.open) {

                    val params = Bundle()
                    params.putString("more_action_click", "Open File")
                    application?.firebaseAnalytics?.logEvent("Bookmark_Layout", params)

                    onPrepareOpenAds(listData[pos].path!!)
                } else if (item?.itemId == R.id.bookmark) {
                    deleteFromBookmark(listData[pos].path!!)

                    listData.removeAt(pos)
                    adapter.notifyDataSetChanged()

                    val params = Bundle()
                    params.putString("bookmark_file", "0")
                    application?.firebaseAnalytics?.logEvent("Bookmark_Layout", params)

                }else if (item?.itemId == R.id.share) {
                    share(listData[pos].path!!)
                } else if (item?.itemId == R.id.shortcut) {
                    createShortcut(listData[pos].path!!)
                }
                return true
            }

        })

        popup.show() //showing popup menu

    }

    fun onPrepareOpenAds(path: String) {
        Admod.getInstance().forceShowInterstitial(
            context,
            application?.mInterstitialClickOpenAd,
            object : AdCallback() {
                override fun onAdClosed() {
                    gotoViewPdf(path)
                }
            }
        )
    }

    override fun onBookmarkClick(pos: Int) {
        var data = listData[pos]
//        var bookmarkStatus = data.isBookmark!!

//        if (bookmarkStatus){

            deleteFromBookmark(data.path!!)
        listData.removeAt(pos)
        adapter.notifyDataSetChanged()
//        }else{
//            addToBookmark(data.path!!)
//        }

//        data.isBookmark = !bookmarkStatus
//        adapter.notifyDataSetChanged()
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

    fun share(path: String){
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
    fun deleteFromBookmark(path: String) {
        var realm = Realm.getDefaultInstance()

        realm.executeTransaction { realm ->
            val result: RealmResults<BookmarkRealmObject> =
                realm.where(BookmarkRealmObject::class.java).equalTo("path", path).findAll()
            result.deleteAllFromRealm()
        }
    }

    fun gotoViewPdf(path: String) {
        var intent = Intent(context, PdfViewerActivity::class.java)
        intent.putExtra("url", path)
        startActivity(intent)
    }

    fun processData() {
        if (sort == null) {
            sort = SortModel(type = "0", order = "0")
        }

        if (sort!!.order.equals("0")){
            if (sort!!.type.equals("0")) {
                listData.sortBy {
                    it.name
                }
            }else if (sort!!.type.equals("1")){
                listData.sortBy {
                    it.size
                }
            }else{
                listData.sortBy {
                    it.lastModifier
                }
            }
        }else{
            if (sort!!.type.equals("0")) {
                listData.sortByDescending {
                    it.name
                }
            }else if (sort!!.type.equals("1")){
                listData.sortByDescending {
                    it.size
                }
            }else{
                listData.sortByDescending {
                    it.lastModifier
                }
            }
        }
    }
}