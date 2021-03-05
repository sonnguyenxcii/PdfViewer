package py.com.opentech.drawerwithbottomnavigation.ui.scan

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import butterknife.BindView
import butterknife.ButterKnife
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import py.com.opentech.drawerwithbottomnavigation.R

class ScanPdfActivity : AppCompatActivity() {

    @BindView(R.id.camera)
    lateinit var camera: AppCompatImageView

    @BindView(R.id.library)
    lateinit var library: AppCompatImageView

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {

        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_scan_pdf_layout)
        ButterKnife.bind(this)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.title = "Máy quét"

        camera.setOnClickListener {
            Dexter.withContext(this)
                .withPermissions(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
//                            showImagePickerOptions34()
                        }
//                        if (report.isAnyPermissionPermanentlyDenied) {
//                            showSettingsDialog()
//                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                        token.continuePermissionRequest()
                    }
                }).check()
        }

        library.setOnClickListener {

        }
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
}