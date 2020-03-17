package com.example.cian.screens.addeditad

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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cian.R
import com.example.cian.models.Ad
import com.example.cian.screens.main.arrayItemId
import com.example.cian.utils.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.fragment_add_edit_ad.*
import kotlinx.android.synthetic.main.progress_bar.*

const val REQUEST_GALLERY_PICTURE = 1

class AddEditAdFragment : Fragment(R.layout.fragment_add_edit_ad), AdapterView.OnItemSelectedListener,
    OnMapReadyCallback {

    companion object {
        private val TAG = AddEditAdFragment::class.java.simpleName
    }

    private lateinit var firebase: FirebaseHelper
    private lateinit var currentUserUid: String
    private lateinit var map: GoogleMap
    private lateinit var adId: String
    private val viewModel by activityViewModels<AddEditViewModel>()
    private val args by navArgs<AddEditAdFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebase = FirebaseHelper()
        if (savedInstanceState == null) viewModel.clear()
        Log.d(TAG, "onCreate: ")
        args.postId?.let { adId = it }
    }

    override fun onStart() {
        super.onStart()
        firebase.auth.addAuthStateListener { auth ->
            if (auth.currentUser == null) navigateBack()
            auth.currentUser?.let { currentUserUid = it.uid }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        adaptersSetup()


        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (savedInstanceState == null && ::adId.isInitialized && viewModel.ad.value == null) {
            firebase.databaseAd().child(args.postId!!)
                .addListenerForSingleValueEvent(ValueEventListenerAdapter { dataAd ->
                    val ad = dataAd.getValue(Ad::class.java)
                    viewModel.updateAd(ad)
                    ad?.images?.forEach { viewModel.updateImages(it.value, ImageState.Done) }
                })
        }

        viewModel.ad.observe(viewLifecycleOwner, Observer { ad -> ad?.let { updateFields(it) } })
        viewModel.images.observe(viewLifecycleOwner, Observer { checkChangeImagesUrisNumber(it) })
        viewModel.adState.observe(viewLifecycleOwner, Observer { postStateActions(it) })
        new_ad_photo_text.setOnClickListener { takePicture() }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.add, menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_add -> {
                hideKeyboard(requireActivity())
                checkPostValue()
            }
            else -> false
        }

    private fun checkPostValue(): Boolean {
        Log.d(TAG, "menu clicked")
        when (viewModel.adState.value) {
            AdState.NOTHING -> viewModel.updateAdState(
                AdState.LOADING
            )
            AdState.ERROR -> viewModel.updateAdState(
                AdState.LOADING
            )
            else -> return false
        }
        return true
    }

    private fun postStateActions(it: AdState?) {
        when (it) {
            AdState.NOTHING -> progress_bar.hideView()
            AdState.LOADING -> checkAddOrEditAd()
            AdState.ERROR -> progress_bar.hideView()
            AdState.DONE -> postUploadedSuccessfully()
            else -> Log.e(TAG, "postState is null")
        }
    }

    private fun checkAddOrEditAd() = if (::adId.isInitialized) editAd() else shareAd()

    private fun shareAd() {
        Log.d(TAG, "sharePost called")

        progress_bar.showView {}

        val databaseReference = firebase.databaseAd().push().apply {
            this.key?.let { adId = it }
        }

        when {
            areInputsBlank() -> {
                showToast(R.string.check_fields)
                viewModel.updateAdState(AdState.NOTHING)
            }
            (::adId.isInitialized) -> uploadAdInDatabase(databaseReference) {
                Log.d(TAG, "post uploaded successfully")
                if (viewModel.images.value.isNullOrEmpty()) viewModel.updateAdState(
                    AdState.DONE
                )

                viewModel.images.value?.forEach { entry ->
                    val image = entry.key as Uri
                    Log.d(TAG, image.toString())
                    uploadImageInStrorageAndDatabase(entry, image)
                }
            }
        }
    }

    private fun editAd() {
        Log.d(TAG, "updateAd: called")

        progress_bar.showView {}
        firebase.updateAd(adId, createUpdateMap(createAd())) {
            viewModel.images.value?.let { entry ->
                if (entry.isNullOrEmpty() || entry.all { it.value == ImageState.Done }) {
                    viewModel.updateAdState(AdState.DONE)
                }
            }

            viewModel.images.value?.forEach { entry ->
                Log.d(TAG, entry.value.toString())
                when (entry.value) {
                    ImageState.Delete -> {
                        deleteImageFromStorageAndDatabase(entry.key as String)
                    }
                    ImageState.Upload -> {
                        val image = entry.key as Uri
                        uploadImageInStrorageAndDatabase(entry, image)
                    }
                    ImageState.Done -> {
                    }
                }
            }

        }
    }

    private fun deleteImageFromStorageAndDatabase(imageUrl: String) {
        firebase.databaseImage(adId).child(imageUrl).removeValue()
        firebase.storageImage(imageUrl).delete()
    }

    private fun uploadImageInStrorageAndDatabase(
        entry: Map.Entry<Any, ImageState>,
        image: Uri
    ) {
        uploadImage(entry) { imageUrl ->
            updateDatabaseImages(imageUrl) {
                Log.d(TAG, "complete")
                viewModel.updateImages(image, ImageState.Done)
            }
        }
    }

    private fun createUpdateMap(ad: Ad) = mutableMapOf<String, Any?>().apply {
        this["shortDescription"] = ad.shortDescription
        this["fullDescription"] = ad.fullDescription
        this["typeOfHousing"] = ad.typeOfHousing
        this["numberOfRoom"] = ad.numberOfRoom
        this["typeAd"] = ad.typeAd
        this["price"] = ad.price
    }

    private fun checkChangeImagesUrisNumber(imagesUris: MutableMap<Any, ImageState>) {
        if (viewModel.adState.value == AdState.LOADING) {
            checkUploadDone(imagesUris)
        } else {
            new_ad_recycler.adapter =
                AddEditAdAdapter(imagesUris.filter { it.value != ImageState.Delete }
                    .map { it.key }) {
                    when (viewModel.images.value?.get(it)) {
                        ImageState.Upload -> viewModel.deleteImage(it)
                        ImageState.Done -> viewModel.updateImages(it, ImageState.Delete)
                        else -> Log.e(TAG, "images is null")
                    }
                }
        }
    }

    private fun checkUploadDone(imagesUris: MutableMap<Any, ImageState>) {
        if (!imagesUris.isNullOrEmpty() && imagesUris.all { it.value == ImageState.Done })
            viewModel.updateAdState(AdState.DONE)
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
        if (requestCode == REQUEST_GALLERY_PICTURE && resultCode == RESULT_OK && data != null) {
            if (data.clipData != null) {
                val images = data.clipData!!
                val totalItemSelected = images.itemCount

                for (i in 0 until totalItemSelected) {
                    val image = images.getItemAt(i).uri
                    viewModel.updateImages(image, ImageState.Upload)
                }
            } else {
                if (data.data != null) {
                    val image = data.data!!
                    viewModel.updateImages(image, ImageState.Upload)
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {}

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        if (new_ad_type_of_housing.selectedItemPosition == 2) {
            new_ad_number_of_room.run {
                setSelection(0)
                isEnabled = false
            }
        } else {
            new_ad_number_of_room.isEnabled = true
        }
    }

    private fun uploadAdInDatabase(databaseReference: DatabaseReference, onSuccess: () -> Unit) {
        firebase.databaseUserAd(currentUserUid).push().setValue(adId)
        val post = createAd()
        databaseReference.setValue(post)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                errorMessage(it, TAG)
                viewModel.updateAdState(AdState.ERROR)
            }
    }

    private fun createAd(): Ad {
        return Ad(
            shortDescription = new_ad_short_description_input.text.toString().trim(),
            fullDescription = new_ad_full_description_input.text.toString().trim(),
            typeOfHousing = new_ad_type_of_housing.selectedItem.toString(),
            numberOfRoom = new_ad_number_of_room.selectedItem.toString(),
            typeAd = new_ad_type_ad.selectedItem.toString(),
            price = new_ad_price_input.text.toLongOrZero()
        )
    }

    private fun areInputsBlank(): Boolean =
        when {
            new_ad_short_description_input.text.isNullOrBlank() -> true
            new_ad_full_description_input.text.isNullOrBlank() -> true
            new_ad_type_of_housing.selectedItem.toString().isBlank() -> true
            new_ad_number_of_room.selectedItem.toString().isBlank() -> true
            new_ad_type_ad.selectedItem.toString().isBlank() -> true
            new_ad_price_input.text.isNullOrBlank() -> true
            else -> false
        }

    private fun uploadImage(entry: Map.Entry<Any, ImageState>, onSuccess: (String) -> Unit) {
        val uri = entry.key as Uri

        uri.lastPathSegment?.let {
            val storageReference = firebase.storageImage(it)

            val uploadTask = storageReference.putFile(uri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) task.exception?.let { exception -> throw exception }
                storageReference.downloadUrl
            }.addOnSuccessListener { storageImageUri ->
                onSuccess(storageImageUri.toString())
            }.addOnFailureListener { exception ->
                errorMessage(exception, TAG)
                viewModel.updateAdState(AdState.ERROR)
            }
        }
    }

    private fun updateDatabaseImages(imageUrl: String, onSuccess: () -> Unit) {
        firebase.databaseImage(adId).push().setValue(imageUrl)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                errorMessage(it, TAG)
                viewModel.updateAdState(AdState.ERROR)
            }
    }

    private fun postUploadedSuccessfully() {
        progress_bar_fab.showView {
            viewModel.clear()
            progress_bar.hideView()
            if (args.title == getString(R.string.edit_ad_title)) navigateBack() else {
                findNavController().navigate(
                    AddEditAdFragmentDirections.actionNewAdToAdDetail(
                        adId
                    )
                )
            }
        }
    }

    private fun adaptersSetup() {
        createAdapter(new_ad_type_of_housing, R.array.type_of_housing)
        createAdapter(new_ad_number_of_room, R.array.number_of_room)
        createAdapter(new_ad_type_ad, R.array.type_ad)

        new_ad_type_of_housing.onItemSelectedListener = this
        new_ad_number_of_room.onItemSelectedListener = this
        new_ad_type_ad.onItemSelectedListener = this
    }

    private fun createAdapter(spinner: Spinner, array: Int) {
        ArrayAdapter.createFromResource(
            requireContext(), array, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    private fun updateFields(post: Ad?) {
        if (post != null) {
            resources.getStringArray(R.array.type_ad).indexOf("Post")
            new_ad_short_description_input.setText(post.shortDescription)
            new_ad_full_description_input.setText(post.fullDescription)
            new_ad_type_of_housing
                .setSelection(arrayItemId(R.array.type_of_housing, post.typeOfHousing))
            new_ad_number_of_room
                .setSelection(arrayItemId(R.array.number_of_room, post.numberOfRoom))
            new_ad_type_ad.setSelection(arrayItemId(R.array.type_ad, post.typeAd))
            new_ad_price_input.setText(post.price.toStringOrBlank())
        }
    }

    private fun navigateBack() {
        requireActivity().onBackPressed()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: ")
        if (::adId.isInitialized && viewModel.ad.value == null) return
        if (viewModel.adState.value != AdState.DONE) viewModel.updateAd(createAd())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        val googleMapOption = GoogleMapOptions().liteMode(true)
        map.mapType = googleMapOption.mapType
    }
}