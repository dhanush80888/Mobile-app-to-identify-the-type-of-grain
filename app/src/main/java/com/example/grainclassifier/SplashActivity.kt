package com.example.grainclassifier

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.grainclassifier.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup ViewBinding
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize state for programmatic entrance animations
        binding.logoCard.alpha = 0f
        binding.logoCard.scaleX = 0.7f
        binding.logoCard.scaleY = 0.7f
        binding.tvTitle.alpha = 0f
        binding.tvTitle.translationY = 30f
        binding.tvSubtitle.alpha = 0f
        binding.tvSubtitle.translationY = 20f
        binding.loadingProgress.alpha = 0f
        binding.tvLoadingText.alpha = 0f

        // Execute beautiful entrance transitions
        binding.logoCard.animate()
            .alpha(1f)
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(1000)
            .setInterpolator(DecelerateInterpolator())
            .start()

        binding.tvTitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(300)
            .setInterpolator(DecelerateInterpolator())
            .start()

        binding.tvSubtitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(800)
            .setStartDelay(550)
            .setInterpolator(DecelerateInterpolator())
            .start()

        // Fade in bottom loading indicator gently after 1 second
        binding.loadingProgress.animate()
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(1000)
            .start()

        binding.tvLoadingText.animate()
            .alpha(0.8f)
            .setDuration(500)
            .setStartDelay(1200)
            .start()

        // Transition to MainActivity after loading sequence completes (2.5 seconds)
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            
            // Finish this splash screen so the user cannot back-navigate to it
            finish()
            
            // Apply professional crossfade window transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, 2500)
    }
}
