package py.com.opentech.drawerwithbottomnavigation.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.lang.reflect.Modifier


@Parcelize
data class BookModel(

    var gutenberg_id: String? = "",
    var title: String? = "",
    var descriptions: List<String>? = null,
    var author: AuthorModel? = null,
    var images_base_url: String? = "",
    var images: List<BookImageModel>? = null,
    var subjects: List<String>? = null,
    var category: List<String>? = null,
    var formats: List<FormatModel>? = null,


) : Parcelable