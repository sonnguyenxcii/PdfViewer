package py.com.opentech.drawerwithbottomnavigation.ui.tools

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_book_detail.*
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.api.ApiService
import py.com.opentech.drawerwithbottomnavigation.model.BookModel
import py.com.opentech.drawerwithbottomnavigation.model.BookRequestModel

class BookDetailActivity : AppCompatActivity() {
    protected var compositeDisposable = CompositeDisposable()
    var bookModel: BookModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setTitle("")
//        toolbar.setNavigationOnClickListener(object : OnClickListener() {
//            fun onClick(view: View?) {
//                //do something you want
//            }
//        })
        getBookDetail()

        downloadBtn.setOnClickListener {
            getBookUrl()
        }

        readmore.setOnClickListener {
            descriptionText.onChange()
            if (descriptionText.isTrim) {
                readmore.setText("Read more")
            } else {
                readmore.setText("Show less")

            }
        }
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

    fun getBookDetail() {
        var application = PdfApplication.create(this)
        val peopleService: ApiService = application.jsonService
        val disposable: Disposable = peopleService.getBookDetail(getId())
            .subscribeOn(application.subscribeScheduler())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ responseBody ->
                if (responseBody != null) {
                    bookModel = responseBody
                    bindData(bookModel!!)
                }
            }) { throwable ->

            }

        compositeDisposable.add(disposable)
    }

    fun getBookUrl() {
        if (bookModel?.formats == null) {
            return
        }

        var application = PdfApplication.create(this)
        val peopleService: ApiService = application.bhpService
        val disposable: Disposable =
            peopleService.getBookUrl(BookRequestModel(url = bookModel!!.formats!![0].file_url))
                .subscribeOn(application.subscribeScheduler())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ responseBody ->
                    if (responseBody != null) {

                    }
                }) { throwable ->

                }

        compositeDisposable.add(disposable)
    }

    fun bindData(book: BookModel) {
        author.setText(book.author?.name)
        descriptionText.setText(book.descriptions!![2])
        name.setText(book.title)
        var imageUrl = ""
        try {
            book.images?.forEach {
                if (it.size == "large") {
                    imageUrl = it.amazon_url!!
                }
            }
            println("--------------------------" + imageUrl)
        } catch (e: Exception) {
            println("---e-----------------------" + imageUrl)

        }
        Glide.with(this).load(imageUrl)
            .placeholder(R.drawable.ic_ebook_placeholder)
            .into(cover)

        mTagContainerLayout.setTags(book.category)
    }
}