package com.example.cian.extesions

fun Long.toStringOrBlank() = if (this == 0L) "" else this.toString()