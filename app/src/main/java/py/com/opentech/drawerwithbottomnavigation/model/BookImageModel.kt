package py.com.opentech.drawerwithbottomnavigation.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.lang.reflect.Modifier


@Parcelize
data class BookImageModel(

    var asin: String? = "",
    var file_name: String? = "",
    var amazon_url: String? = "",
    var size: String? = ""
) : Parcelable