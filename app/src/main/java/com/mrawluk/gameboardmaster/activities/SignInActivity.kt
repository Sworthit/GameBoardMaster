package com.mrawluk.gameboardmaster.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.google.firebase.auth.FirebaseAuth
import com.mrawluk.gameboardmaster.R
import com.mrawluk.gameboardmaster.databinding.ActivitySignInBinding
import com.mrawluk.gameboardmaster.firebase.FirestoreBase
import com.mrawluk.gameboardmaster.models.User

class SignInActivity : BaseActivity() {
    private var binding: ActivitySignInBinding? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        auth = FirebaseAuth.getInstance()
        setUpActionBar()

        binding?.signInBtn?.setOnClickListener {
            signIn()
        }
    }

    private fun setUpActionBar() {
        setSupportActionBar(binding?.signInToolbar)
        val actionBar = supportActionBar

        if(actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_black_back_button)
        }

        binding?.signInToolbar?.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun signIn() {
        val email = binding?.etEmail?.text.toString()
        val password = binding?.etPassword?.text.toString()
        if(validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        FirestoreBase().getUser(this)
                    } else {
                        showError(task.exception!!.message.toString())
                    }
                }

        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
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

    fun signInSuccess(user: User) {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}