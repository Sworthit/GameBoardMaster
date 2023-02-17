package com.mrawluk.gameboardmaster.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import com.mrawluk.gameboardmaster.databinding.ActivitySplashScreenBinding
import com.mrawluk.gameboardmaster.firebase.FirestoreBase

class SplashScreenActivity : BaseActivity() {
    private var binding: ActivitySplashScreenBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        window.insetsController?.hide(WindowInsets.Type.statusBars())
        val typeFace: Typeface = Typeface.createFromAsset(assets, "carbon.ttf")
        binding?.tvAppName?.typeface = typeFace

        Handler(Looper.getMainLooper()).postDelayed({
            var currentUserId = FirestoreBase().getCurrentUserId()
            if (currentUserId.isNotEmpty()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }, 3000)
    }
}