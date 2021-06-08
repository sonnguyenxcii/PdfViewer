package py.com.opentech.drawerwithbottomnavigation.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.lang.reflect.Modifier


@Parcelize
data class BookModel(

    var gutenberg_id: String? = "",
    var title: String? = "",
    var author: AuthorModel? = null,
    var images_base_url: String? = "",


) : Parcelable