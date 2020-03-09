package com.example.cian.screens.main

import androidx.fragment.app.Fragment

fun Fragment.arrayItemId(array: Int, value: String): Int {
    return resources.getStringArray(array).indexOf(value)
}


