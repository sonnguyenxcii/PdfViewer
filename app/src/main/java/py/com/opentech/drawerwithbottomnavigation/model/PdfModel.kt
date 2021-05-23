package py.com.opentech.drawerwithbottomnavigation.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.lang.reflect.Modifier


@Parcelize
data class PdfModel(

    var name: String? = "",
    var path: String? = "",
    var size: Long? = 0,
    var date: String? = "",
    var lastModifier: Long? = 0,
    var isCheck: Boolean? = false,
    var isBookmark: Boolean? = false,
    var percent: Int? = 0

) : Parcelable