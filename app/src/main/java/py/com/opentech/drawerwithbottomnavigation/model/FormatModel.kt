package py.com.opentech.drawerwithbottomnavigation.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.lang.reflect.Modifier


@Parcelize
data class FormatModel(

    var file_url: String? = "",
    var format_name: String? = ""


) : Parcelable