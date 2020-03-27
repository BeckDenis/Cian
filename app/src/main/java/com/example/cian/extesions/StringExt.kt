package com.example.cian.extesions

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import java.util.*


fun String.toAddress(context: Context?): String {
    try {
        val (lat, lng) = this.split(",").map { it.toDouble() }
        return Geocoder(context, Locale.getDefault())
            .getFromLocation(lat, lng, 1)[0]
            .getAddressLine(0)
    } catch (e: Exception) {
        Log.e("StringExt", "toAddress: ${e.message}" )
    }
    return ""
}

fun String.toLatLng(): LatLng {
    try {
        val (lat, lng) = this.split(",").map { it.toDouble() }
        return LatLng(lat, lng)
    } catch (e: Exception) {
        Log.e("StringExt", "toLatLng: ${e.message}" )
    }
    return LatLng(0.0,0.0)
}