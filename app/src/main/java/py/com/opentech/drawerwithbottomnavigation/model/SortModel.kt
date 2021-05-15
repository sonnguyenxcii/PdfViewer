package py.com.opentech.drawerwithbottomnavigation.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

// type : 0-name, 1-size, 2- date
// order : 0-inc, 1-desc
@Parcelize
data class SortModel(
    var type: String? = "",
    var order: String? = ""

) : Parcelable