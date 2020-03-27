package com.example.cian.screens.ads

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs

import com.example.cian.R
import com.example.cian.models.Ad
import com.example.cian.utils.FirebaseHelper
import com.example.cian.utils.ValueEventListenerAdapter
import kotlinx.android.synthetic.main.fragment_ads.*

class AdsFragment : Fragment(R.layout.fragment_ads) {

    companion object {
        private val TAG = AdsFragment::class.java.simpleName
    }

    private lateinit var firebase: FirebaseHelper
    private val args by navArgs<AdsFragmentArgs>()
    private val viewModel by activityViewModels<AdsViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebase = FirebaseHelper()

        if (savedInstanceState == null) {
            if (args.adsId != null) {
                args.adsId?.toList()?.let {
                    viewModel.adsIdsSize = it.size
                    getAds(it)
                }
            } else currentUserAdsIds()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.ads.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "${it.size} ${viewModel.adsIdsSize}")
            if (it.size == viewModel.adsIdsSize) updateRecycler(it)
        })
    }

    private fun updateRecycler(posts: MutableSet<Ad>) {
        Log.d(TAG, "posts: $posts")
        if (posts.isNotEmpty()) {
            posts_recycler.adapter = AdsAdapter(posts.toList().reversed()) { adId ->
                navigateToDetail(adId)
            }
            Log.d(TAG, "updateRecycler called")
        }
    }

    private fun navigateToDetail(adId: String) {
        val action = AdsFragmentDirections.actionAdsToAdDetail(adId)
        findNavController().navigate(action)
    }

    private fun getAds(adsIds: List<String>) {
        Log.d(TAG, "getPosts called")
        adsIds.forEach { postId ->
            firebase.databaseAd().child(postId)
                .addListenerForSingleValueEvent(ValueEventListenerAdapter { adData ->
                    val ad = adData.getValue(Ad::class.java)?.copy(id = adData.key!!)
                    if (ad != null) {
                        viewModel.updatePosts(ad)
                    }
                })
        }
    }

    private fun currentUserAdsIds() {
        firebase.currentUser?.let { user ->
            firebase.databaseUserAd(user.uid)
                .addListenerForSingleValueEvent(ValueEventListenerAdapter { adsIdsData ->
                    val adsIds = adsIdsData.children.map { it.value as String }
                    viewModel.adsIdsSize = adsIds.size
                    getAds(adsIds)
                })
        }
    }

}
