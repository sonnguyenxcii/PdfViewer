package py.com.opentech.drawerwithbottomnavigation.ui.merge

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dmax.dialog.SpotsDialog
import org.apache.pdfbox.io.MemoryUsageSetting
import org.apache.pdfbox.multipdf.PDFMergerUtility
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import py.com.opentech.drawerwithbottomnavigation.ui.home.RecycleViewOnClickListener
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfViewerActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.String


class MultiFileSelectActivity : AppCompatActivity(), RecycleViewOnClickListener {
    var listData: ArrayList<PdfModel> = ArrayList()
    lateinit var adapter: MultiFileSelectAdapter
    protected var application: PdfApplication? = null
    var hideIcon: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_file)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Select the file"
        val recyclerView: RecyclerView = findViewById(R.id.recycleView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MultiFileSelectAdapter(this, listData, this)
        recyclerView.adapter = adapter
        application = PdfApplication.create(this)

        application?.global?.listData?.observe(this, androidx.lifecycle.Observer {
            if (it != null) {
                listData.clear()
                listData.addAll(it)
                adapter.notifyDataSetChanged()
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.done -> {
                onDone()
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun onDone() {
        showInputName()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_select_multi_file, menu)
        menu.findItem(R.id.done).isVisible = !hideIcon
        return true
    }

    override fun onItemClick(pos: Int) {
        var data = listData[pos]
        data.isCheck = !data.isCheck!!
        adapter.notifyItemChanged(pos)
        caculateSelectCount()

    }

    override fun onMoreClick(pos: Int, view: View) {

    }

    override fun onBookmarkClick(pos: Int) {

    }

    fun caculateSelectCount() {
        var count = 0
        listData.forEach {
            if (it.isCheck!!) {
                count++
            }
        }
        hideIcon = count < 2

        invalidateOptionsMenu()

    }

    var fileName = ""
    fun showInputName() {

        val view = layoutInflater.inflate(R.layout.dialog_input_name, null)
        val categoryEditText = view.findViewById(R.id.categoryEditText) as EditText
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setTitle("Create PDF")
            .setMessage("Input name of file")
            .setView(view)
            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                val task = String.valueOf(categoryEditText.text)
                if (!TextUtils.isEmpty(task)) {
                    fileName = task
                    downloadAndCombinePDFs()
                }

            })
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    @Throws(IOException::class)
    private fun downloadAndCombinePDFs(): File {
        val dialog: android.app.AlertDialog? = SpotsDialog.Builder().setContext(this).build()
        dialog?.show()

        val file = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
            "$fileName.pdf"
        )

        Thread {
            val ut = PDFMergerUtility()
            listData.forEach {
                if (it.isCheck!!) {
                    var fileTemp = File(it.path)
                    ut.addSource(fileTemp)
                }
            }

            if (file.exists())
                file.delete()

            val fileOutputStream = FileOutputStream(file)
            try {
                ut.destinationStream = fileOutputStream
                ut.mergeDocuments(MemoryUsageSetting.setupTempFileOnly())
            } catch (e: Exception) {
                e.printStackTrace()
                e.message?.let { showAlertDialog(it) }
            } finally {
                fileOutputStream.close()
                dialog?.dismiss()

            }

            gotoViewPdf(file.absolutePath)
        }.start()


        println("---------------" + file.absolutePath)
        return file
    }

    fun gotoViewPdf(path: kotlin.String) {
        var intent = Intent(this, PdfViewerActivity::class.java)
        intent.putExtra("url", path)
        startActivity(intent)
        finish()
    }

    fun showAlertDialog(msg: kotlin.String) {
        if (isFinishing) return
        try {
            val dialogBuilder = android.app.AlertDialog.Builder(this)
            dialogBuilder.setTitle(null)
            dialogBuilder.setIcon(R.mipmap.ic_launcher)
            dialogBuilder.setMessage(msg)
            dialogBuilder.setPositiveButton(
                "OK"
            ) { dialog: DialogInterface?, which: Int ->}
            dialogBuilder.setCancelable(false)
            dialogBuilder.show()
        } catch (e: java.lang.Exception) {
//            Debug.e("---  Exception BaseActivity " + e.message)
        }
    }
}