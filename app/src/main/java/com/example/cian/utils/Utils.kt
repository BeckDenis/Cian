package com.example.cian.utils

import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.lang.Exception

fun Fragment.errorMessage(it: Exception, tag: String) {
    Log.d(tag, it.message.toString())
    Toast.makeText(requireContext(), it.localizedMessage, Toast.LENGTH_SHORT).show()
}