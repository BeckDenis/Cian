package com.example.cian.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

private const val TAG = "Utils"

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

fun setAddress(map: GoogleMap, latLng: LatLng) {
    Log.d(TAG, "setAddress: called")
    val zoomLvl = 15f
    map.run {
        moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLvl))
        addMarker(MarkerOptions().position(latLng)).showInfoWindow()
    }

}