package com.example.cian.screens.main


import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cian.R
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * A simple [Fragment] subclass.
 */
class MainFragment : Fragment(R.layout.fragment_main) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        main_place_an_ad_button.setOnClickListener { goToNewAd() }
    }

    private fun goToNewAd() {
        findNavController().navigate(MainFragmentDirections.actionMainToNewAd())
    }
}
