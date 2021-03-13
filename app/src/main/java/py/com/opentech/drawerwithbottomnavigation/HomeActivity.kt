package py.com.opentech.drawerwithbottomnavigation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.google.android.material.navigation.NavigationView
import com.infideap.drawerbehavior.AdvanceDrawerLayout
import kotlinx.android.synthetic.main.app_bar_default.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import py.com.opentech.drawerwithbottomnavigation.model.FileChangeEvent
import py.com.opentech.drawerwithbottomnavigation.model.PdfModel
import py.com.opentech.drawerwithbottomnavigation.ui.merge.MergePdfActivity
import py.com.opentech.drawerwithbottomnavigation.ui.scan.ScanPdfActivity
import java.io.File


class HomeActivity : AppCompatActivity(),
    NavigationView.OnNavigationItemSelectedListener {

    private var drawer: AdvanceDrawerLayout? = null
    private lateinit var navController: NavController
    private val MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
    protected var application: PdfApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advance5)
        application = PdfApplication.create(this)

        this.application?.global?.isListMode?.postValue(true)

        setupNavController()

        drawer = findViewById<View>(R.id.drawer_layout) as AdvanceDrawerLayout

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)
        drawer!!.setViewScale(Gravity.START, 0.9f)
        drawer!!.setRadius(Gravity.START, 0f)
        drawer!!.setViewElevation(Gravity.START, 20f)

        bottomNavigationView.background = null
        bottomNavigationView.menu.getItem(1).isEnabled = false

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            navigateTo(item.itemId)
            true
        }

        homeBtn.setOnClickListener {
            if (drawer!!.isDrawerOpen(GravityCompat.START)) {
                drawer!!.closeDrawer(GravityCompat.START)
            } else {
                drawer!!.openDrawer(GravityCompat.START)

            }
        }

        fab.setOnClickListener {
            navigateTo(R.id.nav_bookmark)
            bottomNavigationView.selectedItemId = R.id.nav_bookmark
        }

        mode.setOnClickListener {
            var temp = this.application?.global?.isListMode?.value
            this.application?.global?.isListMode?.postValue(!temp!!)
        }

        application?.global?.isListMode?.observe(this, Observer {
            if (it != null) {
                if (it) {
                    mode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mode_grid))
                } else {
                    mode.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_mode_list))

                }
            }
        })
    }

    private fun setupNavController() {
        navController = findNavController(R.id.nav_host_fragment)
    }

    private fun navigateTo(resId: Int) {
        navController.navigate(resId)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(noteEvent: FileChangeEvent) {
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

    override fun onResume() {
        super.onResume()
        requestRead()

    }

    override fun onBackPressed() {
        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
            drawer!!.closeDrawer(GravityCompat.START)
        } else {
            finish()
//            super.onBackPressed()
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_home -> {
//                Toast.makeText(this, "file", Toast.LENGTH_SHORT).show()
//                drawer!!.openDrawer(GravityCompat.END)
                drawer!!.closeDrawer(GravityCompat.START)
                return false

            }
            R.id.nav_file_manager -> {
                Toast.makeText(this, "file", Toast.LENGTH_SHORT).show()
//                drawer!!.openDrawer(GravityCompat.END)
                drawer!!.closeDrawer(GravityCompat.START)
                return false

            }
            R.id.nav_pdf_scan -> {
                navigateToScan()
                drawer!!.closeDrawer(GravityCompat.START)
                return false
            }

            R.id.nav_pdf_merge -> {
                navigateToMerge()

                drawer!!.closeDrawer(GravityCompat.START)
                return false
            }

            R.id.nav_feedback -> {
                Toast.makeText(this, "feedback", Toast.LENGTH_SHORT).show()
                drawer!!.closeDrawer(GravityCompat.START)
                return false
            }
            R.id.nav_share -> {
                Toast.makeText(this, "share", Toast.LENGTH_SHORT).show()
                drawer!!.closeDrawer(GravityCompat.START)
                return false
            }
        }
//        drawer!!.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
//            R.id.nav_gallery -> {
//                Toast.makeText(this, "gallery", Toast.LENGTH_SHORT).show()
////                drawer!!.openDrawer(GravityCompat.END)
//                return true
//            }
        }
        return super.onOptionsItemSelected(item)
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
            .onEnter {
                !it.isHidden // it is not hidden
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
        Thread {
            var listData = getExternalPDFFileList()
            application?.global?.listData?.postValue(listData)
        }.start()


    }
}