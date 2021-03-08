package py.com.opentech.drawerwithbottomnavigation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.bottom_navigation_view.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import py.com.opentech.drawerwithbottomnavigation.model.FileChangeEvent
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import py.com.opentech.drawerwithbottomnavigation.ui.merge.MergePdfActivity
import py.com.opentech.drawerwithbottomnavigation.ui.scan.ScanPdfActivity
import java.io.File


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    protected var application: PdfApplication? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        application = PdfApplication.create(this)

        setSupportActionBar(toolbar)
        setupNavController()
        setupDrawerNavigation()
        setupBottomNavigation()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(noteEvent: FileChangeEvent) {
        requestRead()

    }

    override fun onResume() {
        super.onResume()
        requestRead()

    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
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

                R.id.nav_scan -> {
                    navigateToScan()
                }
                R.id.nav_merge -> {
                    navigateToMerge()
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
        val uriList: ArrayList<PdfModel> = ArrayList()

        val ROOT_DIR = Environment.getExternalStorageDirectory().absolutePath
        val ANDROID_DIR = File("$ROOT_DIR/Android")
        val DATA_DIR = File("$ROOT_DIR/data")
        File(ROOT_DIR).walk()
            // befor entering this dir check if
            .onEnter{ !it.isHidden // it is not hidden
                    && it != ANDROID_DIR // it is not Android directory
                    && it != DATA_DIR // it is not data directory
                    && !File(it, ".nomedia").exists() //there is no .nomedia file inside
            }.filter { it.extension == "pdf" }
            .toList().forEach {
                uriList.add(PdfModel(it.name, it.absolutePath, it.length()))
            }

        return uriList
    }

    fun requestRead() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            readFile()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readFile()
            } else {

            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    fun readFile() {
        Thread{
            var listData = getExternalPDFFileList()
            application?.global?.listData?.postValue(listData)
        }.start()


    }

}
