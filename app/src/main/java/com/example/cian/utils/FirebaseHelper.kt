package com.example.cian.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class FirebaseHelper {

    companion object {
        private val TAG = FirebaseHelper::class.java.simpleName
    }

    val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance().reference
    val currentUser = auth.currentUser

    fun storageImage(name: String) = storage.child("ads/images/$name")

    fun updateAd(adId: String, updates: Map<String, Any?>, onSuccess: () -> Unit) {
        database.child("ads/$adId").updateChildren(updates)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    onSuccess()
                } else {
                    Log.e(TAG, it.exception?.message ?: return@addOnCompleteListener)
                }
            }
    }

    fun databaseImage(adId: String) = database.child("ads/$adId/images")
    fun databaseAd() = database.child("ads")
    fun databaseUserAd(userUid: String) = database.child("userAds/$userUid")
    fun databaseAds() = database.child("ads/")

    fun checkAuth() = auth.currentUser != null
}