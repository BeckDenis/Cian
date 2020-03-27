package com.example.cian.screens.detail

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cian.R
import com.example.cian.extesions.toAddress
import com.example.cian.extesions.toLatLng
import com.example.cian.models.Ad
import com.example.cian.utils.FirebaseHelper
import com.example.cian.utils.ValueEventListenerAdapter
import com.example.cian.utils.ViewPagerAdapter
import com.example.cian.utils.setAddress
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.database.DataSnapshot
import kotlinx.android.synthetic.main.fragment_ad_detail.*

class AdDetailFragment : Fragment(R.layout.fragment_ad_detail), OnMapReadyCallback {

    companion object {
        private val TAG = AdDetailFragment::class.java.simpleName
    }

    private val args by navArgs<AdDetailFragmentArgs>()
    private lateinit var firebase: FirebaseHelper
    private lateinit var map: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebase = FirebaseHelper()

        firebase.currentUser?.let { user ->
            firebase.databaseUserAd(user.uid)
                .addListenerForSingleValueEvent(ValueEventListenerAdapter { adsIdsData ->
                    val adsIds = adsIdsData.children.map { it.getValue(String::class.java) }
                    if (adsIds.contains(args.adId)) setHasOptionsMenu(true)
                })
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: called")
        firebase.databaseAd().child(args.adId)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter { data ->
                updateView(data)
            })

        (childFragmentManager.findFragmentById(R.id.ad_detail_map) as SupportMapFragment).apply {
            getMapAsync(this@AdDetailFragment)
        }
    }

    private fun updateView(data: DataSnapshot) {
        val post = data.getValue(Ad::class.java)
        post?.let { ad ->
            ad_detail_short_description_text.text = ad.shortDescription
            ad_detail_full_description_text.text = ad.fullDescription
            ad_detail_price_text.text = ad.price.toString()
            ad_detail_address_text.text = ad.latLng.toAddress(context)

            val adapter = ViewPagerAdapter(requireContext(), ad.images?.map { it.value }) {}
            post_detail_view_pager.adapter = adapter

            if (::map.isInitialized) setAddress(map, ad.latLng.toLatLng())
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.edit, menu)

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.menu_edit -> {
                val action = AdDetailFragmentDirections.actionAdDetailToAddEditAd(
                    args.adId, getString(R.string.edit_ad_title)
                )
                findNavController().navigate(action)
                true
            }
            else -> false
        }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
    }

}
