package com.mrawluk.taskmaster.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.mrawluk.taskmaster.R
import com.mrawluk.taskmaster.databinding.ActivitySignInBinding
import com.mrawluk.taskmaster.firebase.FireStoreBase
import com.mrawluk.taskmaster.models.User

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
                        FireStoreBase().getUser(this)
                    } else {
                        Toast.makeText(this, task.exception.toString(), Toast.LENGTH_LONG).show()
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