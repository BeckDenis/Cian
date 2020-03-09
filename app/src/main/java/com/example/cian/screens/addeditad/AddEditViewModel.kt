package com.example.cian.screens.addeditad

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.cian.models.Post
import com.example.cian.models.PostState

class AddEditViewModel : ViewModel() {

    private var _post = MutableLiveData<Post>()
    val post: LiveData<Post>
        get() = _post

    private var _postState = MutableLiveData(PostState.NOTHING)
    val postState: LiveData<PostState>
        get() = _postState

    private var _imagesUris = MutableLiveData(HashMap<Uri, Boolean>())
    val imagesUris: LiveData<HashMap<Uri, Boolean>>
        get() = _imagesUris

    fun updatePost(post: Post) {
        _post.value = post
    }

    fun updatePostState(state: PostState) {
        _postState.value = state
    }

    fun updateImagesUris(key: Uri, value: Boolean) {
        _imagesUris.value?.set(key, value)
        _imagesUris.notifyObserver()
    }

    fun deleteImageUri(key: Uri) {
        _imagesUris.value?.remove(key)
        _imagesUris.notifyObserver()
    }

    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    fun clear() {
        _imagesUris.value = HashMap()
        _postState.value = PostState.NOTHING
    }

}