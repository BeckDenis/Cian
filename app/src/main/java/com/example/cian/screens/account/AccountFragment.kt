package com.example.cian.screens.account

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.cian.R
import com.example.cian.models.Ad
import com.example.cian.screens.ads.AdsAdapter
import com.example.cian.utils.FirebaseHelper
import com.example.cian.utils.ValueEventListenerAdapter
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.fragment_account.*

class AccountFragment : Fragment(R.layout.fragment_account) {

    companion object {
        private val TAG = AccountFragment::class.java.simpleName
    }

    private lateinit var firebase: FirebaseHelper
    private val viewModel by activityViewModels<AccountViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: ")
        firebase = FirebaseHelper()
        getUserAdsIdsAndAds()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTextView()
        viewModel.ads.observe(viewLifecycleOwner, Observer { updateRecyclerView(it) })
    }

    private fun updateRecyclerView(ads: MutableSet<Ad>) {
        account_recycler.adapter = AdsAdapter(ads.toList().reversed()) { adId ->
            val action = AccountFragmentDirections.actionAccountToAdDetail(adId)
            findNavController().navigate(action)
        }
    }

    private fun getUserAdsIdsAndAds() {
        firebase.auth.currentUser?.let { user ->
            getUserAdsIds(user) { getAds(it) }
        }
    }

    private fun getUserAdsIds(user: FirebaseUser, adsId: (List<String>) -> Unit) {
        firebase.databaseUserAd(user.uid)
            .addListenerForSingleValueEvent(ValueEventListenerAdapter { adsData ->
                adsId(adsData.children.map { it.value as String })
            })
    }

    private fun getAds(adsId: List<String>) {
        adsId.forEach { adId ->
            firebase.databaseAd().child(adId)
                .addListenerForSingleValueEvent(ValueEventListenerAdapter { adData ->
                    adData.getValue(Ad::class.java)?.copy(id = adData.key!!)?.let {
                        viewModel.updateAds(it)
                    }
                })
        }
    }

    private fun updateTextView() {
        account_name.text = firebase.auth.currentUser?.displayName
        account_email.text = firebase.auth.currentUser?.email
    }

}
