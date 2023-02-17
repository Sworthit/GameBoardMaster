package com.mrawluk.gameboardmaster.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mrawluk.gameboardmaster.R
import com.mrawluk.gameboardmaster.databinding.ActivitySingUpBinding
import com.mrawluk.gameboardmaster.firebase.FirestoreBase
import com.mrawluk.gameboardmaster.models.User

class SignUpActivity : BaseActivity() {
    private var binding: ActivitySingUpBinding? = null
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingUpBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        setContentView(binding?.root)

        setUpActionBar()
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding?.signUpToolbar)

        val actionBar = supportActionBar

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_button)
        }

        binding?.signUpToolbar?.setNavigationOnClickListener {
            onBackPressed()
        }

        binding?.signUpBtn?.setOnClickListener {
            registerUser()
        }
    }

    private fun registerUser() {
        val name: String = binding?.etName?.text.toString().trim{ it <= ' '}
        val email: String = binding?.etEmail?.text.toString().trim{ it <= ' '}
        val password: String = binding?.etPassword?.text.toString()
        if(validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.createUserWithEmailAndPassword (
                email, password
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val firebaseUser: FirebaseUser = task.result!!.user!!
                    val registeredEmail = firebaseUser.email!!
                    val user = User(firebaseUser.uid, registeredEmail, name)
                    FirestoreBase().registerUser(this, user)
                } else {
                    showError(task.exception!!.message.toString())
                    hideProgressDialog()
                }
            }
        }
    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) -> {
                showError("Please enter name")
                false
            }
            TextUtils.isEmpty(email) -> {
                showError("Please enter email")
                false
            }
            TextUtils.isEmpty(password) -> {
                showError("Please enter a password")
                false
            } else -> {
                true
            }
        }
    }

    fun userRegisteredSuccess() {
        Toast.makeText(this, "Successfully registered", Toast.LENGTH_LONG).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }
}