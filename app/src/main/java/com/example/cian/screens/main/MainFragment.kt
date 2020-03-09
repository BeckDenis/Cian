package com.example.cian.screens.main


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cian.R
import com.example.cian.utils.FirebaseHelper
import kotlinx.android.synthetic.main.fragment_main.*

class MainFragment : Fragment(R.layout.fragment_main) {

    private lateinit var firebase: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebase = FirebaseHelper()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        main_place_an_ad_button.setOnClickListener { goToNewAd() }
    }

    private fun goToNewAd() {
        if (firebase.checkAuth()) {
            val action = MainFragmentDirections.actionMainToAddEditAd(
                null, getString(R.string.new_ad_title)
            )
            findNavController().navigate(action)
        } else {
            val action = MainFragmentDirections.actionMainToLoginDialog()
            findNavController().navigate(action)
        }
    }
}
