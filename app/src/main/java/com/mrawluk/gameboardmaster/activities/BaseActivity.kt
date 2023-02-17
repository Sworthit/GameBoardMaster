package com.mrawluk.gameboardmaster.activities

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.mrawluk.gameboardmaster.R
import com.mrawluk.gameboardmaster.utils.Constants
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.net.URL

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

    inner class CallAPIServerAsyncTask() {
        fun startApiCall(title: String, message: String, token: String) {
            lifecycleScope.launch {
                makeApiCall(title, message, token)
            }
        }

        fun makeApiCall(title: String, message: String, token: String)  {
            try {
                val url = URL(Constants.FCM_BASE_URL)
                val jsonRequest = JSONObject()
                jsonRequest.put("to", token)
                jsonRequest.put("notification",
                    JSONObject().also {
                        it.put(Constants.FCM_KEY_TITLE, title)
                        it.put(Constants.FCM_KEY_MESSAGE, message)
                    })

                val request = Request.Builder()
                    .url(url)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "bearer ${Constants.FCM_SERVER_KEY}")
                    .post(
                        jsonRequest.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                    )
                    .build()
                val client = OkHttpClient()

                client.newCall(request).enqueue(
                    object : Callback {
                        override fun onResponse(call: Call, response: Response) {
                        }

                        override fun onFailure(call: Call, e: IOException) {
                        }
                    }
                )
            }  catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}