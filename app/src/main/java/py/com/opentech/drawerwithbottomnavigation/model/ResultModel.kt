package py.com.opentech.drawerwithbottomnavigation.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.lang.reflect.Modifier


@Parcelize
data class ResultModel(

    var books: List<BookModel>? = null,
    var heading: String? = ""

) : Parcelable