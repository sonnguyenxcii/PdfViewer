package py.com.opentech.drawerwithbottomnavigation

import android.Manifest
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.bottom_navigation_view.*
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import py.com.opentech.drawerwithbottomnavigation.ui.merge.MergePdfActivity
import py.com.opentech.drawerwithbottomnavigation.ui.scan.ScanPdfActivity

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    var hdtgAssetModel: MutableLiveData<PdfModel> = MutableLiveData<PdfModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        setupNavController()
        setupDrawerNavigation()
        setupBottomNavigation()
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupNavController() {
        navController = findNavController(R.id.nav_host_fragment)
    }

    private fun setupDrawerNavigation() {
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
//                R.id.nav_tools,
                R.id.nav_share, R.id.nav_send
            ),
            drawer_layout //Note that we use kotlinx.android.synthetic to get the drawerLayout reference in the xml
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        // Note that we use kotlinx.android.synthetic to get the NavigationView reference in the xml
        nav_view.setupWithNavController(navController)
        // Add this listener to the DrawerLayout because besides navigating to the
        // corresponding view we also need to mark the bottom button as checked
        nav_view.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    // mark the bottom button as checked
                    nav_home_button.isChecked = true
                    navigateTo(R.id.nav_home)
                }
                R.id.nav_gallery -> {
                    nav_gallery_button.isChecked = true
                    navigateTo(R.id.nav_gallery)
                }
                R.id.nav_slideshow -> {
                    nav_slideshow_button.isChecked = true
                    navigateTo(R.id.nav_slideshow)
                }
//                R.id.nav_tools -> {
//                    nav_tools_button.isChecked = true
//                    navigateTo(R.id.nav_tools)
//                }

                R.id.nav_scan -> {
                    navigateToScan()
//                    Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show()
//                    nav_tools_button.isChecked = true
//                    navigateTo(R.id.nav_tools)
                }
                R.id.nav_merge -> {
                    navigateToMerge()
//                    Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show()
//                    nav_tools_button.isChecked = true
//                    navigateTo(R.id.nav_tools)
                }
            }
            drawer_layout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun setupBottomNavigation() {
        nav_home_button.setOnClickListener {
            navigateTo(R.id.nav_home)
        }
        nav_gallery_button.setOnClickListener {
            navigateTo(R.id.nav_gallery)
        }
        nav_slideshow_button.setOnClickListener {
            navigateTo(R.id.nav_slideshow)
        }
//        nav_tools_button.setOnClickListener {
//            navigateTo(R.id.nav_tools)
//        }
    }

    private fun navigateTo(resId: Int) {
        navController.navigate(resId)
    }

    private fun navigateToScan() {

        var intent = Intent(this, ScanPdfActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToMerge() {

        var intent = Intent(this, MergePdfActivity::class.java)
        startActivity(intent)
    }


    private fun getExternalPDFFileList(): ArrayList<PdfModel> {
        val cr: ContentResolver = getContentResolver()!!
        val uri: Uri = MediaStore.Files.getContentUri("external")
        val projection =
            arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.DISPLAY_NAME,
                MediaStore.Files.FileColumns.SIZE
            )
//        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
//                + MediaStore.Files.FileColumns.MEDIA_TYPE)
//        val selectionArgs: Array<String>? = null
        val selectionMimeType = MediaStore.Files.FileColumns.MIME_TYPE + "=?"
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension("pdf")
        val selectionArgsPdf = arrayOf(mimeType)
        val cursor: Cursor = cr.query(uri, projection, selectionMimeType, selectionArgsPdf, null)!!
        val uriList: ArrayList<PdfModel> = ArrayList()
        cursor.moveToFirst()
        while (!cursor.isAfterLast()) {
            val columnIndex: Int = cursor.getColumnIndex(projection[0])
            val fileId: Long = cursor.getLong(columnIndex)
            val fileUri: Uri = Uri.parse(uri.toString().toString() + "/" + fileId)
            val displayName: String = cursor.getString(cursor.getColumnIndex(projection[1]))
            val size: Long = cursor.getLong(cursor.getColumnIndex(projection[2]))
            uriList.add(PdfModel(displayName, fileUri.toString(), size))
            cursor.moveToNext()
            println("--displayName----------------------" + displayName)
        }
        cursor.close()
        println("--uriList----------------------" + uriList.size)


        return uriList
    }
    fun requestRead() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            readFile()
        }
    }
}
