package py.com.opentech.drawerwithbottomnavigation.ui.recent

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
import py.com.opentech.drawerwithbottomnavigation.model.realm.RecentRealmObject
import py.com.opentech.drawerwithbottomnavigation.ui.home.HomeAdapter
import py.com.opentech.drawerwithbottomnavigation.ui.home.RecycleViewOnClickListener
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfViewerActivity
import py.com.opentech.drawerwithbottomnavigation.utils.Constants
import java.io.File


class RecentFragment : Fragment(), RecycleViewOnClickListener {

    var listData: ArrayList<PdfModel> = ArrayList()
    lateinit var adapter: RecentAdapter
    protected var application: PdfApplication? = null
    var isListMode: Boolean = false
    var sort: SortModel? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_recent, container, false)
        val recyclerView: RecyclerView = root.findViewById(R.id.recycleView)
        recyclerView.layoutManager =
            if (isListMode) LinearLayoutManager(requireContext()) else GridLayoutManager(
                requireContext(),
                2
            )
        Admod.getInstance().loadSmallNativeFragment(activity, Constants.ADMOB_Native_Recently, root)

        adapter = RecentAdapter(requireContext(), listData, this)
        recyclerView.adapter = adapter
        application = PdfApplication.create(activity)
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
        return root
    }

    override fun onResume() {
        super.onResume()
        listData.clear()
        var models = getAllRecent()

        models?.forEach { bm ->
            application?.global?.listData?.value?.forEach { data ->
                if (bm?.path?.equals(data.path)!!) {
                    var model = getBookmarkByPath(data.path!!)
                    data.isBookmark = !model.isNullOrEmpty()

                    try {
                        data.currentPage = bm.page
                        data.totalPage = bm.totalPage
                    } catch (e: Exception) {
                        data.currentPage = 0
                        data.totalPage = 0

                    }

                    listData.add(data)
                    processData()
                }
            }
        }

        adapter.notifyDataSetChanged()
    }

    fun getAllRecent(): List<RecentRealmObject?>? {
        var realm = Realm.getDefaultInstance()

        return realm.where(RecentRealmObject::class.java).findAll()
    }

    override fun onItemClick(pos: Int) {

        val params = Bundle()
        params.putString("button_click", "Open File")
        application?.firebaseAnalytics?.logEvent("Recently_Layout", params)

//        Admod.getInstance().forceShowInterstitial(
//            context,
//            application?.mInterstitialAd,
//            object : AdCallback() {
//                override fun onAdClosed() {
//
//                    gotoViewPdf(listData[pos].path!!)
//                }
//            }
//        )
        onPrepareOpenAds(listData[pos].path!!)

    }

    override fun onMoreClick(pos: Int, view: View) {
        //Creating the instance of PopupMenu

        val popup = PopupMenu(requireContext(), view)
        //Inflating the Popup using xml file
        popup.menuInflater
            .inflate(R.menu.poupup_recent_menu, popup.getMenu())

        //registering popup with OnMenuItemClickListener

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {

                if (item?.itemId == R.id.open) {

                    val params = Bundle()
                    params.putString("more_action_click", "Open File")
                    application?.firebaseAnalytics?.logEvent("Recently_Layout", params)

                    onPrepareOpenAds(listData[pos].path!!)
                } else if (item?.itemId == R.id.delete) {
                    deleteFromRecent(listData[pos].path!!)

                    listData.removeAt(pos)
                    adapter.notifyDataSetChanged()

                } else if (item?.itemId == R.id.bookmark) {
                    addToBookmark(listData[pos].path!!)

                } else if (item?.itemId == R.id.share) {
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
        var bookmarkStatus = data.isBookmark!!
        val params = Bundle()

        if (bookmarkStatus) {
            params.putString("bookmark_file", "0")

            deleteFromBookmark(data.path!!)

        } else {
            params.putString("bookmark_file", "1")

            addToBookmark(data.path!!)
        }
        application?.firebaseAnalytics?.logEvent("Recently_Layout", params)

        data.isBookmark = !bookmarkStatus
        adapter.notifyItemChanged(pos)
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
        try {
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
        }catch (e:Exception){

        }

    }

    fun addToBookmark(path: String) {
        var model = getBookmarkByPath(path)
        if (model.isNullOrEmpty()) {
            saveBookmark(path)
        }
        Toast.makeText(context, "Added to bookmark", Toast.LENGTH_SHORT).show()
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

    fun deleteFromRecent(path: String) {
        var realm = Realm.getDefaultInstance()

        realm.executeTransaction { realm ->
            val result: RealmResults<RecentRealmObject> =
                realm.where(RecentRealmObject::class.java).equalTo("path", path).findAll()
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
}