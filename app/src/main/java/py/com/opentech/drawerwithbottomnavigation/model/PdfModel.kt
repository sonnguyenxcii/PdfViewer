package py.com.opentech.drawerwithbottomnavigation.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class PdfModel(

    var name: String? = "",
    var path: String? = "",
    var size: Long? = 0,
    var date: String? = "",
    var isCheck: Boolean? = false,
    var isBookmark: Boolean? = false

) : Parcelable