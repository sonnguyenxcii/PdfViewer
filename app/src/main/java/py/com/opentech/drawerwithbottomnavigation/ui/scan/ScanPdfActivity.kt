package py.com.opentech.drawerwithbottomnavigation.ui.scan

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import dmax.dialog.SpotsDialog
import kotlinx.android.synthetic.main.activity_scan_pdf_layout.*
import py.com.opentech.drawerwithbottomnavigation.PdfApplication
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfViewerActivity
import py.com.opentech.drawerwithbottomnavigation.utils.ImagePickerActivity
import java.io.File
import java.io.FileOutputStream
import java.lang.String


class ScanPdfActivity : AppCompatActivity() {

    var RATIO_X = 2
    var RATIO_Y = 3
    private var currentAvatar: Uri? = null
    var hideIcon: Boolean = true
    protected var application: PdfApplication?  = PdfApplication.create(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_pdf_layout)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "PDF Scanner"

        camera.setOnClickListener {

            val params = Bundle()
            params.putString("button_click", "Camera")
            application?.firebaseAnalytics?.logEvent("PDF_Scanner_Layout", params)

            Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            launchCameraIntent()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest>,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
        }

        library.setOnClickListener {
            val params = Bundle()
            params.putString("button_click", "Image")
            application?.firebaseAnalytics?.logEvent("PDF_Scanner_Layout", params)
            Dexter.withContext(this)
                .withPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            launchGalleryIntent()
                        }
//                        if (report.isAnyPermissionPermanentlyDenied) {
//                            showSettingsDialog()
//                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        permissions: List<PermissionRequest>,
                        token: PermissionToken
                    ) {
                        token.continuePermissionRequest()
                    }
                }).check()
        }
    }

    private fun launchCameraIntent() {
        val intent = Intent(this, ImagePickerActivity::class.java)
        intent.putExtra(
            ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION,
            ImagePickerActivity.REQUEST_IMAGE_CAPTURE
        )

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true)
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, RATIO_X) // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, RATIO_Y)

        // setting maximum bitmap width and height
        intent.putExtra(ImagePickerActivity.INTENT_SET_BITMAP_MAX_WIDTH_HEIGHT, true)
//        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 600)
//        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 600)


//        startForResult.launch(intent)
        startActivityForResult(intent, 4)
    }

    private fun launchGalleryIntent() {
        val intent = Intent(this, ImagePickerActivity::class.java)
        intent.putExtra(
            ImagePickerActivity.INTENT_IMAGE_PICKER_OPTION,
            ImagePickerActivity.REQUEST_GALLERY_IMAGE
        )

        // setting aspect ratio
        intent.putExtra(ImagePickerActivity.INTENT_LOCK_ASPECT_RATIO, true)
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_X, RATIO_X) // 16x9, 1x1, 3:4, 3:2
        intent.putExtra(ImagePickerActivity.INTENT_ASPECT_RATIO_Y, RATIO_Y)
//        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_WIDTH, 600)
//        intent.putExtra(ImagePickerActivity.INTENT_BITMAP_MAX_HEIGHT, 600)
        startActivityForResult(intent, 4)
//        startForResult.launch(intent)

    }
//    private fun showSettingsDialog() {
//        val builder = AlertDialog.Builder(this)
//        builder.setTitle(getString(R.string.dialog_permission_title))
//        builder.setMessage(getString(R.string.dialog_permission_message))
//        builder.setPositiveButton(getString(R.string.go_to_settings)) { dialog: DialogInterface, which: Int ->
//            dialog.cancel()
//            openSettings()
//        }
//        builder.setNegativeButton(getString(android.R.string.cancel)) { dialog: DialogInterface, which: Int -> dialog.cancel() }
//        builder.show()
//    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 4) {
            if (resultCode == Activity.RESULT_OK) {
                currentAvatar = data!!.getParcelableExtra("path")
//                if (typeUpload == 2) {
//                    mDKSHTruocUri = currentAvatar
                preview.visibility = View.VISIBLE
                Glide.with(this).load(currentAvatar.toString()).into(preview)
                hideIcon = false

                invalidateOptionsMenu()
            }
        }
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
        val params = Bundle()
        params.putString("button_click", "Create")
        application?.firebaseAnalytics?.logEvent("PDF_Scanner_Layout", params)
        showInputName()

//        downloadAndCombinePDFs()
    }

    fun convertButton() {
        val dialog: android.app.AlertDialog? = SpotsDialog.Builder().setContext(this).build()

        dialog?.show()

        Thread {
            try {
                println("--------currentAvatar.toString()---------" + currentAvatar.toString())
                //        String file = directory + “3.jpg”;
                val bitmap =
                    BitmapFactory.decodeStream(contentResolver.openInputStream(currentAvatar!!))
                println("-----byteCount---------------" + bitmap.byteCount)
//            var bitmap = BitmapFactory.decodeFile(currentAvatar.toString());

                var pdfDocument = PdfDocument()
                var myPageInfo =
                    PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
                var page = pdfDocument.startPage(myPageInfo)

                page.canvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDocument.finishPage(page)

                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath)
                if (!dir.exists()) {
                    dir.mkdir()
                }
                val myPDFFile = File(
                    dir,
                    "$fileName.pdf"
                )
                try {
                    pdfDocument.writeTo(FileOutputStream(myPDFFile))
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                pdfDocument.close()
                dialog?.dismiss()

                gotoViewPdf(myPDFFile.absolutePath)

            } catch (e: Exception) {
                dialog?.dismiss()

                println("--------Exception---------")
                e.printStackTrace()
            }

        }.start()


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

                val params = Bundle()
                params.putString("button_click", "OK")
                application?.firebaseAnalytics?.logEvent("PDF_Scanner_Layout", params)

                val task = String.valueOf(categoryEditText.text)
                if (!TextUtils.isEmpty(task)) {
                    fileName = task
                    convertButton()
                }

            })
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }


    fun gotoViewPdf(path: kotlin.String) {
        var intent = Intent(this, PdfViewerActivity::class.java)
        intent.putExtra("url", path)
        startActivity(intent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_select_multi_file, menu)
        menu.findItem(R.id.done).isVisible = !hideIcon
        return true
    }
}