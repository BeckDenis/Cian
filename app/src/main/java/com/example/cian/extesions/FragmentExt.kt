package com.example.cian.extesions

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.lang.Exception

fun Fragment.showToast(text: Int, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(requireContext(), text, duration).show()
}

fun Fragment.errorMessage(it: Exception, tag: String) {
    Log.d(tag, it.message.toString())
    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
}

fun Fragment.arrayItemId(array: Int, value: String): Int {
    return resources.getStringArray(array).indexOf(value)
}