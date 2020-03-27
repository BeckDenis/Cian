package com.example.cian.screens.addeditad

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import com.example.cian.R
import com.example.cian.extesions.toStringForData
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    companion object {
        private val TAG = MapFragment::class.java.simpleName
    }

    private val viewModel by activityViewModels<AddEditViewModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        Log.d(TAG, "onMapReady: ")
            setMapLongClickListener(map)
    }

    private fun setMapLongClickListener(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            viewModel.updateAd(viewModel.ad.value?.copy(latLng = latLng.toStringForData()))
            activity?.onBackPressed()
        }
    }

}
