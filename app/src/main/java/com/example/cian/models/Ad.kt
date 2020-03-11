package com.example.cian.models

import android.net.Uri

data class Ad(
    val id: String = "",
    val shortDescription: String = "",
    val fullDescription: String = "",
    val images: HashMap<String, String>? = null,
    val typeOfHousing: String = "",
    val numberOfRoom: String = "",
    val price: Long = 0L,
    val typeAd: String = ""
)