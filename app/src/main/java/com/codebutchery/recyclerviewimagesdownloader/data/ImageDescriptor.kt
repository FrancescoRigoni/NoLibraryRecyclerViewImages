package com.codebutchery.recyclerviewimagesdownloader.data

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ImageDescriptor(
        @SerializedName("name") @Expose var name: String?,
        @SerializedName("author") @Expose var author: String?,
        @SerializedName("url") @Expose var url: String?)