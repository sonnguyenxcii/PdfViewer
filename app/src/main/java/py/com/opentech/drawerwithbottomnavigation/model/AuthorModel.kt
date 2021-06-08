package py.com.opentech.drawerwithbottomnavigation.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.lang.reflect.Modifier


@Parcelize
data class AuthorModel(

    var name: String? = "",
    var wikipage: String? = ""


) : Parcelable