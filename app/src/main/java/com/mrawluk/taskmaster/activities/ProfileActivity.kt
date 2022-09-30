package com.mrawluk.taskmaster.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mrawluk.taskmaster.R
import com.mrawluk.taskmaster.databinding.ActivityProfileBinding
import com.mrawluk.taskmaster.firebase.FireStoreBase
import com.mrawluk.taskmaster.models.User
import com.mrawluk.taskmaster.utils.Constants
import java.io.IOException


class ProfileActivity : BaseActivity() {
    private var binding: ActivityProfileBinding? = null

    private var selectedImageUri: Uri? = null
    private var downloadableUri: String = ""
    private lateinit var userDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        setUpActionBar()

        FireStoreBase().getUser(this)

        binding?.ivUserImage?.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageGallery(this)
            } else {
                ActivityCompat.requestPermissions(this,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), Constants.READ_STORAGE_PERMISSION_CODE)
            }
        }

        binding?.btnUpdate?.setOnClickListener{
            if (selectedImageUri != null) {
                uploadUserProfileImage()
            }else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUser()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageGallery(this)
            }
        } else {
            Toast.makeText(this, "Permissions denied, please change them in app settings", Toast.LENGTH_LONG).show()
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding?.toolbarProfile)
        val actionBar = supportActionBar

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_button)
            actionBar.title = resources.getString(R.string.profile_name)
        }

        binding?.toolbarProfile?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun uploadUserProfileImage(){
        showProgressDialog(resources.getString(R.string.please_wait))

        if (selectedImageUri != null) {
            val ref: StorageReference =
                FirebaseStorage.getInstance().reference.child("USER_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(selectedImageUri, this))

            ref.putFile(selectedImageUri!!).addOnSuccessListener {
                taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                    uri ->
                    downloadableUri = uri.toString()
                    updateUser()
                }
            }.addOnFailureListener {
                exception ->
                Toast.makeText(this@ProfileActivity,
                exception.message,
                Toast.LENGTH_LONG).show()
            }
            hideProgressDialog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK
            && requestCode == Constants.PICK_IMAGE_REQUEST_CODE
            && data!!.data != null) {
            selectedImageUri = data.data

            try {
                Glide
                    .with(this)
                    .load(selectedImageUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_placeholder)
                    .into(binding?.ivUserImage!!)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun updateUser() {
        val userHashMap = HashMap<String, Any>()
        var changesMade = false

        if (downloadableUri.isNotEmpty() && downloadableUri != userDetails.image) {
            userHashMap[Constants.IMAGE] = downloadableUri
            changesMade = true
        }

        if (binding?.etName?.text.toString() != userDetails.name) {
            userHashMap[Constants.NAME] = binding?.etName?.text.toString()
            changesMade = true
        }

        if (changesMade) {
            FireStoreBase().updateUser(this, userHashMap)
        } else {
            Toast.makeText(this, "NO CHANGES FOUND", Toast.LENGTH_LONG).show()
            hideProgressDialog()
        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    fun setUser(user: User) {
        userDetails = user
        Glide
            .with(this)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_placeholder)
            .into(binding?.ivUserImage!!)

        binding?.etName?.setText(user.name)
        binding?.etEmail?.setText(user.email)

    }
}