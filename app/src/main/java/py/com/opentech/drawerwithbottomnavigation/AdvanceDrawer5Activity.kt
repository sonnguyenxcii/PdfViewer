//package com.infideap.drawerbehaviorexample.drawer
//
//import android.os.Bundle
//import android.view.Gravity
//import android.view.MenuItem
//import android.view.View
//import android.widget.Toast
//import androidx.appcompat.app.ActionBarDrawerToggle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.appcompat.widget.Toolbar
//import androidx.core.view.GravityCompat
//import com.google.android.material.floatingactionbutton.FloatingActionButton
//import com.google.android.material.navigation.NavigationView
//import com.google.android.material.snackbar.Snackbar
//import com.infideap.drawerbehavior.AdvanceDrawerLayout
//import kotlinx.android.synthetic.main.app_bar_default.*
//import py.com.opentech.drawerwithbottomnavigation.R
//
//class AdvanceDrawer5Activity : AppCompatActivity(),
//    NavigationView.OnNavigationItemSelectedListener {
//
//    private var drawer: AdvanceDrawerLayout? = null
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_advance5)
////        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
////        setSupportActionBar(toolbar)
////        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
////        fab.setOnClickListener { view ->
////            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
////                .setAction("Action", null).show()
////        }
//        drawer = findViewById<View>(R.id.drawer_layout) as AdvanceDrawerLayout
////        val toggle = ActionBarDrawerToggle(
////            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
////        )
////        drawer!!.addDrawerListener(toggle)
////        toggle.syncState()
//        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
//        navigationView.setNavigationItemSelectedListener(this)
//        drawer!!.setViewScale(Gravity.START, 0.9f)
//        drawer!!.setRadius(Gravity.START, 0f)
//        drawer!!.setViewElevation(Gravity.START, 20f)
//
//        bottomNavigationView.background = null
//        bottomNavigationView.menu.getItem(1).isEnabled = false
//
//        homeBtn.setOnClickListener {
//            if (drawer!!.isDrawerOpen(GravityCompat.START)) {
//                drawer!!.closeDrawer(GravityCompat.START)
//            } else {
//                drawer!!.openDrawer(GravityCompat.START)
//
//            }
//        }
//    }
//
//    override fun onBackPressed() {
//        if (drawer!!.isDrawerOpen(GravityCompat.START)) {
//            drawer!!.closeDrawer(GravityCompat.START)
//        } else {
//            super.onBackPressed()
//        }
//    }
//
//    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        // Handle navigation view item clicks here.
//        when (item.itemId) {
//            R.id.nav_file_manager -> {
//                Toast.makeText(this,"gallery",Toast.LENGTH_SHORT).show()
////                drawer!!.openDrawer(GravityCompat.END)
//                drawer!!.closeDrawer(GravityCompat.START)
//                return false
//
//            }
//            R.id.nav_pdf_scan -> {
//                Toast.makeText(this,"nav_slideshow",Toast.LENGTH_SHORT).show()
//                drawer!!.closeDrawer(GravityCompat.START)
//                return false
//            }
//
//            R.id.nav_pdf_merge -> {
//                Toast.makeText(this,"nav_slideshow",Toast.LENGTH_SHORT).show()
//                drawer!!.closeDrawer(GravityCompat.START)
//                return false
//            }
//        }
//        drawer!!.closeDrawer(GravityCompat.START)
//        return true
//    }
//
////    override fun onCreateOptionsMenu(menu: Menu): Boolean {
////        menuInflater.inflate(R.menu.main, menu)
////        return super.onCreateOptionsMenu(menu)
////    }
//
//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.nav_gallery -> {
//                Toast.makeText(this,"gallery",Toast.LENGTH_SHORT).show()
////                drawer!!.openDrawer(GravityCompat.END)
//                return true
//            }
//        }
//        return super.onOptionsItemSelected(item)
//    }
//}