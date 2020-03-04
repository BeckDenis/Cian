package com.example.cian.utils

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class FirebaseHelper {
    val database = FirebaseDatabase.getInstance().reference
    val storage = FirebaseStorage.getInstance().reference

    fun storageImage(key: String, name: String?) =
        storage.child("posts/images/${key}/${name}")

    fun databaseImage(key: String) = database.child("posts/$key/images")
}