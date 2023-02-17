package com.mrawluk.gameboardmaster.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mrawluk.gameboardmaster.R
import com.mrawluk.gameboardmaster.databinding.ActivityCreateBoardBinding
import com.mrawluk.gameboardmaster.firebase.FirestoreBase
import com.mrawluk.gameboardmaster.models.Board
import com.mrawluk.gameboardmaster.utils.Constants
import java.io.IOException

class CreateBoardActivity : BaseActivity() {
    private var binding: ActivityCreateBoardBinding? = null
    private var selectedImageUri: Uri? = null
    private lateinit var userName: String

    private var boardImageURL: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        setUpActionBar()
        if(intent.hasExtra(Constants.NAME)) {
            userName = intent.getStringExtra(Constants.NAME).toString()
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding?.toolbarCreateBoard)
        val actionBar = supportActionBar

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_button)
            actionBar.title = resources.getString(R.string.profile_name)
        }

        binding?.toolbarCreateBoard?.setNavigationOnClickListener {
            onBackPressed()
        }

        binding?.ivBoardImage?.setOnClickListener{
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

        binding?.btnCreate?.setOnClickListener {
            if(selectedImageUri != null) {
                showProgressDialog(resources.getString(R.string.please_wait))
                uploadBoardImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
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
                    .into(binding?.ivBoardImage!!)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun createBoard() {
        val assignedUsers: ArrayList<String> = ArrayList()
        assignedUsers.add(getCurrentUserId())
        val boardName = binding?.etBoardName?.text.toString()
        val boardUrl = boardImageURL
        val boardUserName = userName

        var board = Board(
            boardName,
            boardUrl,
            boardUserName,
            assignedUsers
        )

        FirestoreBase().registerBoard(this, board)
    }

    private fun uploadBoardImage() {

        if (boardImageURL != null) {
            showProgressDialog(resources.getString(R.string.please_wait))
            val ref: StorageReference =
                FirebaseStorage.getInstance().reference.child("BOARD_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(selectedImageUri, this))

            ref.putFile(selectedImageUri!!).addOnSuccessListener {
                    taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                        uri ->
                    boardImageURL = uri.toString()
                    createBoard()
                }
            }.addOnFailureListener {
                    exception ->
                Toast.makeText(this@CreateBoardActivity,
                    exception.message,
                    Toast.LENGTH_LONG).show()
            }
            hideProgressDialog()
        }
    }

    fun boardRegisteredSuccess() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

}