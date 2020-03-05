package com.example.cian.utils

import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class FirebaseHelper {
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference

    fun storageImage(key: String, name: String?) =
        storage.child("posts/images/${key}/${name}")

    fun databaseImage(postId: String) = database.child("posts/$postId/images")
    fun databasePost(postId: String) = database.child("posts/$postId")
    fun databasePosts() = database.child("posts")
}