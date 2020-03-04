package com.example.cian.screens.newAd


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.example.cian.R
import com.example.cian.models.Post
import com.example.cian.utils.FirebaseHelper
import com.example.cian.utils.errorMessage
import com.example.cian.utils.hideView
import com.example.cian.utils.showView
import kotlinx.android.synthetic.main.fragment_new_ad.*
import kotlinx.android.synthetic.main.progress_bar.*

const val REQUEST_GALLERY_PICTURE = 1

class NewAdFragment : Fragment(R.layout.fragment_new_ad), AdapterView.OnItemSelectedListener {

    companion object {
        val TAG = NewAdFragment::class.java.simpleName
    }

    private var imagesUris = HashMap<Uri, Boolean>()
    private lateinit var firebase: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebase = FirebaseHelper()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        new_ad_photo_text.setOnClickListener { takePicture() }

        createAdapter(new_ad_type_of_housing, R.array.type_of_housing)
        createAdapter(new_ad_number_of_room, R.array.number_of_room)
        createAdapter(new_ad_type_ad, R.array.type_ad)

        setHasOptionsMenu(true)

        new_ad_type_of_housing.onItemSelectedListener = this
        new_ad_number_of_room.onItemSelectedListener = this
        new_ad_type_ad.onItemSelectedListener = this
    }

    private fun createAdapter(spinner: Spinner, array: Int) {
        ArrayAdapter.createFromResource(
            requireContext(),
            array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    private fun takePicture() {
        val intent = Intent().apply {
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(
            Intent.createChooser(intent, "Select Picture"),
            REQUEST_GALLERY_PICTURE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_GALLERY_PICTURE && resultCode == RESULT_OK) {
            if (data?.clipData != null) {
                val images = data.clipData
                val totalItemSelected = images?.itemCount ?: 0

                for (i in 0 until totalItemSelected) {
                    val image = images?.getItemAt(i)?.uri ?: return
                    imagesUris[image] = false
                    Log.d(TAG, imagesUris.toString())
                }
            } else {
                if (data?.data != null) {
                    val image = data.data ?: return
                    imagesUris[image] = false
                }
            }
        }

        new_ad_recycler.adapter = NewAdAdapter(imagesUris.map { it.key })
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (new_ad_type_of_housing.selectedItem.toString() == "Room") {
            new_ad_number_of_room.run {
                setSelection(0)
                isEnabled = false
            }
        } else {
            new_ad_number_of_room.isEnabled = true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_add -> {
                Log.d(TAG, "menu clicked")
                sharePost()
                true
            }
            else -> false
        }

    private fun sharePost() {
        Log.d(TAG, "sharePost called")
        val ref = firebase.database.child("posts").push()
        val key = ref.key ?: return
        Log.d(TAG, "ref.key not null")
        progress_bar.showView()
        val post = Post(
            shortDescription = new_ad_short_description_input.text.toString(),
            fullDescription = new_ad_full_description_input.text.toString(),
            typeOfHousing = new_ad_type_of_housing.selectedItem.toString(),
            numberOfRoom = new_ad_number_of_room.selectedItem.toString(),
            typeAd = new_ad_type_ad.selectedItem.toString(),
            price = new_ad_price_input.text.toString().toLong()
        )
        ref.setValue(post)
            .addOnSuccessListener {
                Log.d(TAG, "post upload successfully")
                imagesUris.forEach { uri ->
                    Log.d(TAG, uri.toString())
                    firebase.storageImage(key, uri.key.lastPathSegment).putFile(uri.key)
                        .addOnSuccessListener {
                            Log.d(TAG, "storage success")
                            downloadImage(uri.key, key)
                        }
                        .addOnFailureListener {
                            errorMessage(it, TAG)
                            progress_bar.hideView()
                        }
                }
            }
            .addOnFailureListener {
                errorMessage(it, TAG)
                progress_bar.hideView()
            }
    }


    private fun downloadImage(uri: Uri, key: String) {
        firebase.storageImage(key, uri.lastPathSegment).downloadUrl
            .addOnSuccessListener {
                val imageUrl = it.toString()
                updateDatabase(key, imageUrl, uri)
            }
            .addOnFailureListener {
                errorMessage(it, TAG)
                progress_bar.hideView()
            }
    }

    private fun updateDatabase(key: String, imageUrl: String, uri: Uri) {
        firebase.databaseImage(key).push().setValue(imageUrl)
            .addOnSuccessListener {
                Log.d(TAG, "complete")
                imagesUris[uri] = true
                if (imagesUris.all { it.value }) {
                    progress_bar.hideView()
                    activity?.onBackPressed()
                }
            }
            .addOnFailureListener {
                errorMessage(it, TAG)
                progress_bar.hideView()
            }
    }

}