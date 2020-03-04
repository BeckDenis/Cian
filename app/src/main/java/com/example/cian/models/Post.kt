package com.example.cian.models

import android.net.Uri

data class Post(
    val id: String = "",
    val shortDescription: String = "",
    val fullDescription: String = "",
    val images: List<Uri>? = null,
    val typeOfHousing: String = "",
    val numberOfRoom: String? = null,
    val price: Long = 0L,
    val typeAd: String = ""
)