package com.example.cian.extesions

import android.text.Editable

fun Editable.toLongOrZero() = if (this.toString() == "") 0L else this.toString().toLong()