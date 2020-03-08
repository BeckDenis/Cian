package com.example.cian.screens.newAd

data class NewPost(
    val shortDescription: String = "",
    val fullDescription: String = "",
    val typeOfHousingId: Int = 0,
    val numberOfRoomId: Int = 0,
    val price: String = "",
    val typeAdId: Int = 0
)