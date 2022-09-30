package com.mrawluk.taskmaster.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.mrawluk.taskmaster.R

open class BaseActivity : AppCompatActivity() {

    private var doubleBackToExit = false
    private lateinit var progressDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    fun showProgressDialog(text: String) {
        progressDialog = Dialog(this)

        progressDialog.setContentView(R.layout.dialog_progress)

        progressDialog.findViewById<TextView>(R.id.tvProgressText).text = text

        progressDialog.show()
    }

    fun hideProgressDialog() {
        progressDialog.dismiss()
    }

    fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser!!.uid
    }

    fun doubleBackToExit() {
        if (doubleBackToExit) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExit = true

        Toast.makeText(this,
            "Click back once more to exit the application",
            Toast.LENGTH_LONG)
            .show()

        Handler(Looper.getMainLooper()).postDelayed({doubleBackToExit = false}, 4000)
    }

    fun showError(message: String) {
        val snackBar = Snackbar.make(findViewById(android.R.id.content),
            message,
            Snackbar.LENGTH_LONG)

        val snackBarView = snackBar.view
        snackBarView.setBackgroundColor(
            ContextCompat.getColor(this, R.color.snackBarError))

        snackBar.show()
    }
}