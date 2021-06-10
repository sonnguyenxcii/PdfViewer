package py.com.opentech.drawerwithbottomnavigation.ui.tools

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import butterknife.BindView
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_book_detail.*
import kotlinx.android.synthetic.main.activity_list_book.*
import okhttp3.*
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.api.ApiService
import py.com.opentech.drawerwithbottomnavigation.model.BookModel
import py.com.opentech.drawerwithbottomnavigation.ui.home.HomeAdapter
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfViewerActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class ListBookActivity : AppCompatActivity() {

    protected var compositeDisposable = CompositeDisposable()
    var bookModel: ArrayList<BookModel> = ArrayList()
    var path: String? = null
    var adapter: ListBookAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_book)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setTitle(getId())
        recycleView.layoutManager = LinearLayoutManager(this)

        adapter = ListBookAdapter(this, bookModel)
        recycleView.adapter = adapter

        getListBook()

    }

    fun gotoViewPdf(path: String) {
        var intent = Intent(this, PdfViewerActivity::class.java)
        intent.putExtra("url", path)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun getId(): String? {
        return intent.getStringExtra("id")
    }

    fun getListBook() {
        var application = PdfApplication.create(this)
        val peopleService: ApiService = application.jsonService
        val disposable: Disposable = peopleService.getListBookByCat(getId())
            .subscribeOn(application.subscribeScheduler())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ responseBody ->
                if (responseBody?.data != null) {
                    bookModel.addAll(responseBody.data!!)
                    loadingBookLayout.visibility = View.GONE
                    adapter?.notifyDataSetChanged()
                }
            }) { throwable ->
                throwable.printStackTrace()
            }

        compositeDisposable.add(disposable)
    }

}