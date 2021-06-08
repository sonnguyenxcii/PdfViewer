package py.com.opentech.drawerwithbottomnavigation.model

import com.google.gson.annotations.SerializedName

data class BaseResponseModel<T>(

        @field:SerializedName("Status")
        var status: String? = null,

        @field:SerializedName("Message")
        var message: String? = null,

        @field:SerializedName("results")
        var data: T? = null
)