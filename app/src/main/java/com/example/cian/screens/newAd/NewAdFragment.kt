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
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.cian.MainActivity
import com.example.cian.R
import com.example.cian.models.Post
import com.example.cian.models.PostState
import com.example.cian.utils.*
import kotlinx.android.synthetic.main.fragment_new_ad.*
import kotlinx.android.synthetic.main.progress_bar.*

const val REQUEST_GALLERY_PICTURE = 1

class NewAdFragment : Fragment(R.layout.fragment_new_ad), AdapterView.OnItemSelectedListener {

    companion object {
        val TAG = NewAdFragment::class.java.simpleName
    }

    private lateinit var firebase: FirebaseHelper
    private lateinit var currentUserUid: String
    private lateinit var postId: String
    private val viewModel by activityViewModels<NewAdViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebase = FirebaseHelper()
    }

    override fun onStart() {
        super.onStart()
        firebase.auth.addAuthStateListener {
            if (it.currentUser == null) {
                activity?.onBackPressed()
            } else {
                currentUserUid = it.currentUser!!.uid
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        updateFields()

        adaptersSetup()

        viewModel.imagesUris.observe(viewLifecycleOwner, Observer { updateRecycler(it) })
        viewModel.postState.observe(viewLifecycleOwner, Observer { postStateActions(it) })
        new_ad_photo_text.setOnClickListener { takePicture() }
    }

    private fun postStateActions(it: PostState?) {
        when (it) {
            PostState.NOTHING -> progress_bar.hideView()
            PostState.LOADING -> {
                sharePost()
                progress_bar.showView {}
            }
            PostState.ERROR -> progress_bar.hideView()
            PostState.DONE -> postUploadedSuccessfully()
            else -> Log.e(TAG, "postState is null")
        }
    }

    private fun updateRecycler(imagesUris: HashMap<Uri, Boolean>) {
        Log.d(TAG, "imagesUris: $imagesUris")
        if (!imagesUris.isNullOrEmpty() && imagesUris.all { it.value })
            viewModel.updatePostState(PostState.DONE)

        new_ad_recycler.adapter = NewAdAdapter(imagesUris.map { it.key })
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
                    viewModel.updateImagesUris(image, false)
                }
            } else {
                if (data.data != null) {
                    val image = data.data!!
                    viewModel.updateImagesUris(image, false)
                }
            }
        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) =
        inflater.inflate(R.menu.add, menu)

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_add -> {
                hideKeyboard(activity as MainActivity)
                checkPostValue()
            }
            else -> false
        }

    private fun checkPostValue(): Boolean {
        Log.d(TAG, "menu clicked")
        when (viewModel.postState.value) {
            PostState.NOTHING -> viewModel.updatePostState(PostState.LOADING)
            PostState.ERROR -> viewModel.updatePostState(PostState.LOADING)
            else -> return false
        }
        return true
    }

    private fun sharePost() {
        Log.d(TAG, "sharePost called")

        val databaseReference = firebase.databasePost().push()

        if (checkFields() && databaseReference.key != null) {
            postId = databaseReference.key!!
            firebase.databaseUserPost(currentUserUid).push().setValue(postId)
            val post = createPost()
            databaseReference.setValue(post)
                .addOnSuccessListener {
                    Log.d(TAG, "post uploaded successfully")
                    if (viewModel.imagesUris.value.isNullOrEmpty()) viewModel.updatePostState(
                        PostState.DONE
                    )

                    viewModel.imagesUris.value?.forEach { uri ->
                        Log.d(TAG, uri.toString())
                        if (uri.key.lastPathSegment != null) uploadImage(uri) {
                            updateDatabase(it, uri.key)
                        }
                    }
                }
                .addOnFailureListener {
                    errorMessage(it, TAG)
                    viewModel.updatePostState(PostState.ERROR)
                }
        } else {
            Toast.makeText(context, R.string.check_fields, Toast.LENGTH_LONG).show()
            viewModel.updatePostState(PostState.NOTHING)
        }
    }

    private fun createPost(): Post {
        return Post(
            shortDescription = new_ad_short_description_input.text.toString(),
            fullDescription = new_ad_full_description_input.text.toString(),
            typeOfHousing = new_ad_type_of_housing.selectedItem.toString(),
            numberOfRoom = new_ad_number_of_room.selectedItem.toString(),
            typeAd = new_ad_type_ad.selectedItem.toString(),
            price = new_ad_price_input.text.toString().toLong()
        )
    }

    private fun checkFields(): Boolean =
        when {
            new_ad_short_description_input.text.isNullOrBlank() -> false
            new_ad_full_description_input.text.isNullOrBlank() -> false
            new_ad_type_of_housing.selectedItem.toString().isBlank() -> false
            new_ad_number_of_room.selectedItem.toString().isBlank() -> false
            new_ad_type_ad.selectedItem.toString().isBlank() -> false
            new_ad_price_input.text.isNullOrBlank() -> false
            else -> true
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
            viewModel.updatePostState(PostState.ERROR)
        }
    }

    private fun updateDatabase(imageUrl: String, uri: Uri) {
        firebase.databaseImage(postId).push().setValue(imageUrl)
            .addOnSuccessListener {
                Log.d(TAG, "complete")
                viewModel.updateImagesUris(uri, true)
            }
            .addOnFailureListener {
                errorMessage(it, TAG)
                viewModel.updatePostState(PostState.ERROR)
            }
    }

    private fun postUploadedSuccessfully() {
        progress_bar_fab.showView {
            progress_bar.hideView()
            findNavController().navigate(
                NewAdFragmentDirections.actionNewAdToPostDetail(postId)
            )
            viewModel.clear()
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
            requireContext(),
            array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    private fun updateFields() {
        val post = viewModel.post.value
        if (post != null) {
            new_ad_short_description_input.setText(post.shortDescription)
            new_ad_full_description_input.setText(post.fullDescription)
            new_ad_type_of_housing.setSelection(post.typeOfHousingId)
            new_ad_number_of_room.setSelection(post.numberOfRoomId)
            new_ad_type_ad.setSelection(post.typeAdId)
            new_ad_price_input.setText(post.price)
        }
    }

    private fun createNewPost(): NewPost {
        return NewPost(
            shortDescription = new_ad_short_description_input.text.toString(),
            fullDescription = new_ad_full_description_input.text.toString(),
            typeOfHousingId = new_ad_type_of_housing.selectedItemPosition,
            numberOfRoomId = new_ad_number_of_room.selectedItemPosition,
            typeAdId = new_ad_type_ad.selectedItemPosition,
            price = new_ad_price_input.text.toString()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.updatePost(createNewPost())
    }

}