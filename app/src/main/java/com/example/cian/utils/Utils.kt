package com.example.cian.utils

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.lang.Exception

fun Fragment.errorMessage(it: Exception, tag: String) {
    Log.d(tag, it.message.toString())
    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
}

fun hideKeyboard(activity: Activity) {
    val inputMethodManager =
        activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    // Check if no view has focus
    val currentFocusedView = activity.currentFocus
    currentFocusedView?.let {
        inputMethodManager.hideSoftInputFromWindow(
            currentFocusedView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS
        )
    }
}

fun ImageView.loadImage(image: Uri) =
    ifNotDestroyed {
        GlideApp.with(this).load(image).centerCrop().into(this)
    }

private fun View.ifNotDestroyed(block: () -> Unit) {
    if (!(context as Activity).isDestroyed) {
        block()
    }
}

fun Editable.toLongOrZero() = if (this.toString() == "") 0L else this.toString().toLong()

fun Long.toStringOrBlank() = if (this == 0L) "" else this.toString()

fun Fragment.showToast(text: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), text, duration).show()
}
