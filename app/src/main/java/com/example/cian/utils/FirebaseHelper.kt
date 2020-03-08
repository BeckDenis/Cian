package com.example.cian.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class FirebaseHelper {
    val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference
    val currentUser = auth.currentUser

    fun storageImage(name: String) =
        storage.child("posts/images/$name")

    fun databaseImage(postId: String) = database.child("posts/$postId/images")
    fun databasePost() = database.child("posts")
    fun databaseUserPost(userUid: String) = database.child("userPosts/$userUid")
    fun databasePosts() = database.child("posts")

    fun checkAuth() = auth.currentUser != null
}