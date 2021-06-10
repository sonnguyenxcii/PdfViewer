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
import com.bumptech.glide.Glide
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_book_detail.*
import okhttp3.*
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.api.ApiService
import py.com.opentech.drawerwithbottomnavigation.model.BookModel
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfViewerActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


class BookDetailActivity : AppCompatActivity() {
    protected var compositeDisposable = CompositeDisposable()
    var bookModel: BookModel? = null
    var path: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_detail)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setTitle("")

        getBookDetail()

        downloadBtn.setOnClickListener {
            downloadFile()
        }

        readmore.setOnClickListener {
            descriptionText.onChange()
            if (descriptionText.isTrim) {
                readmore.setText("Read more")
            } else {
                readmore.setText("Show less")

            }
        }

        readBook.setOnClickListener {
            if (!TextUtils.isEmpty(path)) {
                gotoViewPdf(path!!)
            }
        }

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
                    loadingLayout.visibility = View.GONE

                }
            }) { throwable ->

            }

        compositeDisposable.add(disposable)
    }


    fun downloadFile() {
        if (bookModel?.formats == null) {
            return
        }
        showLoadingDialog()
        Thread(Runnable {

            var client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.MINUTES)
                .readTimeout(30, TimeUnit.MINUTES)
                .build();

            val formBody: RequestBody = FormBody.Builder()
                .add("url", bookModel!!.formats!![0].file_url!!)
                .build()

            val request: Request = Request.Builder()
                .url("http://bhpstudiotech.com/epub/convert/url")
                .post(formBody)
                .build()

            try {
                client.newCall(request).enqueue(object : Callback {

                    override fun onResponse(call: Call, response: Response) {
                        println("successful download")

                        val pdfData = response.body?.byteStream()

                        if (pdfData != null) {
                            val file = File(
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
                                "${bookModel!!.title}.pdf"
                            )
                            val fos = FileOutputStream(file)
                            fos.write(response.body!!.bytes())
                            fos.close()

                            path = file.absolutePath

                        } else {

                        }
                        hideDialog()

                        if (!TextUtils.isEmpty(path)) {
                            runOnUiThread {
                                completeTittle.setText("Download Complete: " + bookModel!!.title)
                                completeLayout.visibility = View.VISIBLE
                            }

                        }

                    }

                    override fun onFailure(call: Call, e: IOException) {
                        hideDialog()

                        println("failed to download")
                    }
                })
            } catch (e: Exception) {
                hideDialog()
                e.printStackTrace()
            }
        }).start()

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

    var dialog: AlertDialog? = null

    fun showLoadingDialog() {
        try {
            val view = layoutInflater.inflate(R.layout.dialog_loading, null)
            val tittle = view.findViewById(R.id.tittle) as AppCompatTextView
            tittle.text = bookModel?.title
            dialog = AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .create()
            Objects.requireNonNull(dialog!!.window)
                ?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog!!.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    fun hideDialog() {
        if (dialog != null) {
            dialog!!.dismiss()
        }
    }
}