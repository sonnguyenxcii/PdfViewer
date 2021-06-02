package py.com.opentech.drawerwithbottomnavigation.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.print.PrintAttributes
import android.print.PrintManager
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ImageSpan
import android.util.DisplayMetrics
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import py.com.opentech.drawerwithbottomnavigation.R
import py.com.opentech.drawerwithbottomnavigation.ui.pdf.PdfDocumentAdapter
import java.io.File

object CommonUtils {
    fun convertDpToPixel(dp: Float, context: Context): Float {
        return dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun convertPixelToDp(dimen: Int, context: Context): Int {
        return (context.resources.getDimension(dimen) / context.resources
            .displayMetrics.density).toInt()
    }

    fun menuIconWithText(r: Drawable, title: String): CharSequence {
        r.setBounds(0, 0, r.intrinsicWidth, r.intrinsicHeight)
        val sb = SpannableString("    $title")
        val imageSpan = ImageSpan(r, ImageSpan.ALIGN_BOTTOM)
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return sb
    }

    @JvmStatic
    fun hideKeyboard(activity: Activity) {
        try {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        } catch (ignored: Exception) {
        }
    }

    fun showKeyboard(context: Context, editText: EditText?) {
        val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm?.showSoftInput(editText, 0)
    }

    fun insertMenuItemIcons(context: Context, popupMenu: PopupMenu) {
        val menu = popupMenu.menu
        if (hasIcon(menu)) {
            for (i in 0 until menu.size()) {
                insertMenuItemIcon(context, menu.getItem(i))
            }
        }
    }

    /**
     * @return true if the menu has at least one MenuItem with an icon.
     */
    private fun hasIcon(menu: Menu): Boolean {
        for (i in 0 until menu.size()) {
            if (menu.getItem(i).icon != null) return true
        }
        return false
    }

    /**
     * Converts the given MenuItem's title into a Spannable containing both its icon and title.
     */
    private fun insertMenuItemIcon(context: Context, menuItem: MenuItem) {
        var icon = menuItem.icon

        // If there's no icon, we insert a transparent one to keep the title aligned with the items
        // which do have icons.
        if (icon == null) icon = ColorDrawable(Color.TRANSPARENT)
        val iconSize = context.resources.getDimensionPixelSize(R.dimen.menu_item_icon_size)
        icon.setBounds(0, 0, iconSize, iconSize)
        val imageSpan = ImageSpan(icon)

        // Add a space placeholder for the icon, before the title.
        val ssb = SpannableStringBuilder("       " + menuItem.title)

        // Replace the space placeholder with the icon.
        ssb.setSpan(imageSpan, 1, 2, 0)
        menuItem.title = ssb
        // Set the icon to null just in case, on some weird devices, they've customized Android to display
        // the icon in the menu... we don't want two icons to appear.
        menuItem.icon = null
    }

     fun onFileClick(context: Context, path: String) {

         println("onFileClick-path---------------"+path)
        try {
            val AUTHORITY_APP = "com.pdfreader.scanner.pdfviewer.provider"
            val uri = FileProvider.getUriForFile(context, AUTHORITY_APP, File(path))
            val uris: ArrayList<Uri> = ArrayList()
            uris.add(uri)
            val intent = Intent()
            intent.action = Intent.ACTION_SEND_MULTIPLE
            intent.putExtra(Intent.EXTRA_TEXT, "Upload PDF file")
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            intent.type = "application/pdf"
            intent.setPackage("com.google.android.apps.docs")

            try {
                context.startActivity(
                    Intent.createChooser(
                        intent,
                        "Select app"
                    )
                )
            } catch (e: java.lang.Exception) {
                Toast.makeText(context, "Can not share file now.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
     fun onActionPrint(context: Context, path: String) {
         println("--onActionPrint--------path----"+path)
         try {
             var printManager: PrintManager =
                 context.getSystemService(Context.PRINT_SERVICE) as PrintManager
             var printAdapter =
                 PdfDocumentAdapter(context, path)
             printManager.print(
                 "Document",
                 printAdapter,
                 PrintAttributes.Builder().build()
             );
         } catch (e: Exception) {
             e.printStackTrace()
         }

    }
}