package py.com.opentech.drawerwithbottomnavigation

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ads.control.Admod
import com.ads.control.funtion.AdCallback
import io.realm.Realm
import io.realm.RealmResults
import kotlinx.android.synthetic.main.activity_search.*
import org.greenrobot.eventbus.EventBus
import py.com.opentech.drawerwithbottomnavigation.model.FileChangeEvent
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import py.com.opentech.drawerwithbottomnavigation.model.realm.BookmarkRealmObject
import py.com.opentech.drawerwithbottomnavigation.ui.home.HomeAdapter
import py.com.opentech.drawerwithbottomnavigation.ui.home.RecycleViewOnClickListener
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfViewerActivity
import py.com.opentech.drawerwithbottomnavigation.utils.Constants
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class SearchActivity : AppCompatActivity(), RecycleViewOnClickListener {

    var listData: ArrayList<PdfModel> = ArrayList()
    lateinit var adapter: HomeAdapter
    protected var application: PdfApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        application = PdfApplication.create(this)

        Admod.getInstance().loadSmallNative(this, Constants.ADMOB_Native_Search)
        application?.global?.listData?.value?.let { listData.addAll(it) }
        val recyclerView: RecyclerView = findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HomeAdapter(this, listData, this)
        adapter.isSwitchView = true
        recyclerView.adapter = adapter
        cancel.setOnClickListener {
            finish()
        }

        edtSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                processData()
//                if (TextUtils.isEmpty(s.toString())) {
//                    clear.visibility = View.GONE
//                    content.visibility = View.GONE
//                    listData.clear()
//                    adapter.notifyDataSetChanged()
//                } else {
//                    clear.visibility = View.VISIBLE
//
//                }
            }

        })

        edtSearch.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
//                if (application?.mInterstitialSearchAd != null) {
//                    // Your action on done
//                    Admod.getInstance().forceShowInterstitial(
//                        this,
//                        application?.mInterstitialSearchAd,
//                        object : AdCallback() {
//                            override fun onAdClosed() {
//                                processData()
//                            }
//                        }
//                    )
//                } else {
                    processData()

//                }

                false
            } else false
        })

        clear.setOnClickListener {
            edtSearch.setText("")
            content.visibility = View.GONE

        }
        edtSearch.requestFocus()
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    }

    @SuppressLint("StringFormatInvalid")
    fun processData() {
        var temp = application?.global?.listData?.value?.filter {
            it.name!!.toLowerCase().contains(edtSearch.text.toString().toLowerCase())
        }
//        if (temp.isNullOrEmpty()) {
////            searchResultEmpty.visibility = View.VISIBLE
////            searchResult.visibility = View.GONE
//
////            searchResultEmptyTittle.setText(
////                getString(
////                    R.string.search_result_empty_tittle,
////                    edtSearch.text.toString()
////                )
////            )
//        } else {

//            searchResultEmpty.visibility = View.GONE
//            searchResult.visibility = View.VISIBLE

//            searchResultTittle.setText(
//                getString(
//                    R.string.search_result_tittle,
//                    edtSearch.text.toString()
//                )
//            )

            var count = temp?.size.toString()
            resultCount.setText(getString(R.string.search_result_count, count))

            listData.clear()
            temp.let {
                it?.let { it1 -> listData.addAll(it1) }
                adapter.notifyDataSetChanged()
            }
//        }

//        content.visibility = View.VISIBLE

    }

    fun gotoViewPdf(path: String) {
        var intent = Intent(this, PdfViewerActivity::class.java)
        intent.putExtra("url", path)
        startActivity(intent)
    }

    override fun onItemClick(pos: Int) {
        gotoViewPdf(listData[pos].path!!)

    }

    override fun onMoreClick(pos: Int, view: View) {
        //Creating the instance of PopupMenu
        val popup = PopupMenu(this, view)
        //Inflating the Popup using xml file
        popup.menuInflater
            .inflate(R.menu.poupup_menu, popup.menu)

        popup.setOnMenuItemClickListener(object : PopupMenu.OnMenuItemClickListener {
            override fun onMenuItemClick(item: MenuItem?): Boolean {

                if (item?.itemId == R.id.open) {
                    gotoViewPdf(listData[pos].path!!)
                } else if (item?.itemId == R.id.delete) {
                    onConfirmDelete(pos)
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
            shortcutManager = getSystemService(ShortcutManager::class.java)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (shortcutManager != null) {
                if (shortcutManager.isRequestPinShortcutSupported) {
                    val shortcut = ShortcutInfo.Builder(this, getString(R.string.app_name))
                        .setShortLabel(filename)
                        .setLongLabel(filename)
                        .setIcon(Icon.createWithResource(this, R.drawable.ic_pdf))
                        .setIntent(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(path)
                            )
                        )
                        .build()
                    shortcutManager.requestPinShortcut(shortcut, null)
                } else Toast.makeText(
                    this,
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
            val photoURI = let {
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
//        Toast.makeText(this, "Added to bookmark", Toast.LENGTH_SHORT).show()
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

    fun onConfirmDelete(pos: Int) {
        val alertDialog: AlertDialog
        val dialogBuilder = AlertDialog.Builder(this)
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
            var path = listData[pos].path
            path?.let { deletePdfFile(it) }
            listData.removeAt(pos)
            adapter.notifyDataSetChanged()

            if (listData.isNullOrEmpty()) {
                searchResultEmpty.visibility = View.VISIBLE
                searchResult.visibility = View.GONE

                searchResultEmptyTittle.setText(
                    getString(
                        R.string.search_result_empty_tittle,
                        edtSearch.text.toString()
                    )
                )
            } else {

                searchResultEmpty.visibility = View.GONE
                searchResult.visibility = View.VISIBLE

                searchResultTittle.setText(
                    getString(
                        R.string.search_result_tittle,
                        edtSearch.text.toString()
                    )
                )

                var count = listData.size.toString()
                resultCount.setText(getString(R.string.search_result_count, count))
            }
            alertDialog.dismiss()
        }
        cancel.setOnClickListener { v: View? -> alertDialog.dismiss() }
    }


    fun deletePdfFile(path: String) {
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
}