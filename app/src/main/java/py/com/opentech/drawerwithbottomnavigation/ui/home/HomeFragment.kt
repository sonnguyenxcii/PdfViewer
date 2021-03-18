package py.com.opentech.drawerwithbottomnavigation.ui.home

import android.app.AlertDialog
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
import androidx.appcompat.widget.AppCompatButton
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
import org.greenrobot.eventbus.EventBus
import py.com.opentech.drawerwithbottomnavigation.BuildConfig
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.SearchActivity
import py.com.opentech.drawerwithbottomnavigation.model.FileChangeEvent
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import py.com.opentech.drawerwithbottomnavigation.model.realm.BookmarkRealmObject
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfViewerActivity
import py.com.opentech.drawerwithbottomnavigation.utils.Constants
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class HomeFragment : Fragment(), RecycleViewOnClickListener {

    var listData: ArrayList<PdfModel> = ArrayList()
    lateinit var adapter: HomeAdapter
    protected var application: PdfApplication? = null

    var isListMode: Boolean = false

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
                    println("---------------------" + it.path!!)
                    var model = getBookmarkByPath(it.path!!)
                    it.isBookmark = !model.isNullOrEmpty()
                    println("---------------------" + model?.size)

                }
                listData.addAll(list)
                adapter.notifyDataSetChanged()
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

        searchBox.setOnClickListener {
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
        Admod.getInstance().forceShowInterstitial(
            context,
            application?.mInterstitialAd,
            object : AdCallback() {
                override fun onAdClosed() {
                    gotoViewPdf(listData[pos].path!!)
                }
            }
        )

    }

    override fun onMoreClick(pos: Int, view: View) {
        //Creating the instance of PopupMenu
        val popup = PopupMenu(requireContext(), view)
        //Inflating the Popup using xml file
        popup.menuInflater
            .inflate(R.menu.poupup_menu, popup.menu)

        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {

                if (item?.itemId == R.id.open) {
                    gotoViewPdf(listData[pos].path!!)
                } else if (item?.itemId == R.id.delete) {
                    onConfirmDelete(listData[pos].path!!)
//                    deleteFile(listData[pos].path!!)
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

    override fun onBookmarkClick(pos: Int) {
        var data = listData[pos]
        var bookmarkStatus = data.isBookmark!!

        if (bookmarkStatus) {
            deleteFromBookmark(data.path!!)
        } else {
            addToBookmark(data.path!!)
        }

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
        var intent = Intent(context, PdfViewerActivity::class.java)
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


}