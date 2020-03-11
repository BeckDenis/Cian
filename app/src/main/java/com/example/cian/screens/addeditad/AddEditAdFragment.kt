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
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.cian.R
import com.example.cian.models.Ad
import com.example.cian.models.PostState
import com.example.cian.screens.main.arrayItemId
import com.example.cian.utils.*
import com.google.firebase.database.DatabaseReference
import kotlinx.android.synthetic.main.fragment_new_ad.*
import kotlinx.android.synthetic.main.progress_bar.*
import java.lang.Exception

const val REQUEST_GALLERY_PICTURE = 1

class AddEditAdFragment : Fragment(R.layout.fragment_new_ad), AdapterView.OnItemSelectedListener {

    companion object {
        private val TAG = AddEditAdFragment::class.java.simpleName
    }

    private var imagesUrisCount = 0
    private lateinit var firebase: FirebaseHelper
    private lateinit var currentUserUid: String
    private lateinit var postId: String
    private val viewModel by activityViewModels<AddEditViewModel>()
    private val args by navArgs<AddEditAdFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebase = FirebaseHelper()
        if (args.postId != null) viewModel.clearEditAd()
        Log.d(TAG, "onCreate: ")
        args.postId?.let { postId = it }
    }

    override fun onStart() {
        super.onStart()
        firebase.auth.addAuthStateListener {auth ->
            try {
                if (auth.currentUser == null) navigateToMain()
                auth.currentUser?.let {currentUserUid = it.uid}
            } catch (e: Exception) {
                Log.e(TAG, e.message ?: return@addAuthStateListener)
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        if (args.postId != null) {
            if (savedInstanceState == null) {
                firebase.databasePost().child(args.postId!!)
                    .addListenerForSingleValueEvent(ValueEventListenerAdapter { dataPost ->
                        val post = dataPost.getValue(Ad::class.java)
                        updateFields(post)
                    })
            } else {
                viewModel.editPost.value.let {
                    updateFields(it!!)
                }
            }
        } else {
            updateFields(viewModel.newAd.value)
        }

        adaptersSetup()

        viewModel.imagesNewPost.observe(
            viewLifecycleOwner,
            Observer { checkChangeImagesUrisNumber(it) })
        viewModel.adState.observe(viewLifecycleOwner, Observer { postStateActions(it) })
        new_ad_photo_text.setOnClickListener { takePicture() }
    }

    private fun postStateActions(it: PostState?) {
        when (it) {
            PostState.NOTHING -> progress_bar.hideView()
            PostState.LOADING -> {
                sharePost()
            }
            PostState.ERROR -> progress_bar.hideView()
            PostState.DONE -> postUploadedSuccessfully()
            else -> Log.e(TAG, "postState is null")
        }
    }

    private fun checkChangeImagesUrisNumber(imagesUris: MutableMap<Uri, Boolean>) {
        if (imagesUrisCount == imagesUris.size) {
            checkUploadDone(imagesUris)
        } else {
            imagesUrisCount = imagesUris.size
            new_ad_recycler.adapter = AddEditAdAdapter(imagesUris.map { it.key }) {
                viewModel.deleteNewAdImage(it)
            }
        }
    }

    private fun checkUploadDone(imagesUris: MutableMap<Uri, Boolean>) {
        if (!imagesUris.isNullOrEmpty() && imagesUris.all { it.value })
            viewModel.updateAdState(PostState.DONE)
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
                    viewModel.updateImagesNewAd(image, false)
                }
            } else {
                if (data.data != null) {
                    val image = data.data!!
                    viewModel.updateImagesNewAd(image, false)
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.add, menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_add -> {
                firebase.auth.signOut()
                true
//                hideKeyboard(activity as MainActivity)
//                checkPostValue()
            }
            else -> false
        }

    private fun checkPostValue(): Boolean {
        Log.d(TAG, "menu clicked")
        when (viewModel.adState.value) {
            PostState.NOTHING -> viewModel.updateAdState(PostState.LOADING)
            PostState.ERROR -> viewModel.updateAdState(PostState.LOADING)
            else -> return false
        }
        return true
    }

    private fun sharePost() {
        Log.d(TAG, "sharePost called")

        progress_bar.showView {}

        val databaseReference = firebase.databasePost().push()
        databaseReference.key?.let { postId = it }

        when {
            areInputsBlank() -> {
                showToast(R.string.check_fields)
                viewModel.updateAdState(PostState.NOTHING)
            }
            (::postId.isInitialized) -> uploadPostInDatabase(databaseReference) {
                Log.d(TAG, "post uploaded successfully")
                if (viewModel.imagesNewPost.value.isNullOrEmpty()) {
                    viewModel.updateAdState(PostState.DONE)
                }

                viewModel.imagesNewPost.value?.forEach { uri ->
                    Log.d(TAG, uri.toString())
                    if (uri.key.lastPathSegment != null) uploadImage(uri) { imageUrl ->
                        updateDatabaseImages(imageUrl) {
                            Log.d(TAG, "complete")
                            viewModel.updateImagesNewAd(uri.key, true)
                        }
                    }
                }
            }
        }
    }

    private fun uploadPostInDatabase(databaseReference: DatabaseReference, onSuccess: () -> Unit) {
        firebase.databaseUserPost(currentUserUid).push().setValue(postId)
        val post = createPost()
        databaseReference.setValue(post)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                errorMessage(it, TAG)
                viewModel.updateAdState(PostState.ERROR)
            }
    }

    private fun createPost(): Ad {
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

    private fun uploadImage(uri: Map.Entry<Uri, Boolean>, onSuccess: (String) -> Unit) {
        val storageReference = firebase.storageImage(uri.key.lastPathSegment!!)

        val uploadTask = storageReference.putFile(uri.key)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            storageReference.downloadUrl
        }.addOnSuccessListener { storageImageUri ->
            onSuccess(storageImageUri.toString())
        }.addOnFailureListener {
            errorMessage(it, TAG)
            viewModel.updateAdState(PostState.ERROR)
        }
    }

    private fun updateDatabaseImages(imageUrl: String, onSuccess: () -> Unit) {
        firebase.databaseImage(postId).push().setValue(imageUrl)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                errorMessage(it, TAG)
                viewModel.updateAdState(PostState.ERROR)
            }
    }

    private fun postUploadedSuccessfully() {
        progress_bar_fab.showView {
            viewModel.clearNewAd()
            progress_bar.hideView()
            findNavController().navigate(AddEditAdFragmentDirections.actionNewAdToPostDetail(postId))
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

    private fun navigateToMain() {
        val action = AddEditAdFragmentDirections.actionAddEditAdToMain()
        findNavController().navigate(R.id.main_fragment)
    }

    override fun onDestroyView() {
        Log.d(TAG, "onDestroyView: ")
        if (args.postId != null) {
            viewModel.updateEditAd(createPost())
        } else {
            viewModel.updateNewAd(createPost())
        }
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: ")
    }
}