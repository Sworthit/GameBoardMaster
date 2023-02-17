package com.mrawluk.gameboardmaster.activities

import android.content.Intent
import android.os.Bundle
import com.mrawluk.gameboardmaster.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {
    private var binding: ActivityIntroBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.btnSignUpIntro?.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding?.btnSignInIntro?.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}