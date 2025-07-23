package com.kairaxus.bundymom

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.media.MediaPlayer

class HomeActivity : AppCompatActivity() {

    private lateinit var getStartedButton: Button
    private val imageViews = mutableListOf<ImageView>()
    private val handler = Handler(Looper.getMainLooper())
    private val animationDuration = 4000L // 4 second per image
    private val totalAnimationTime = 20000L // 20 seconds total
    // Add this at the top with other properties
    private lateinit var mediaPlayer: MediaPlayer



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        // In onCreate(), after setContentView()
        mediaPlayer = MediaPlayer.create(this, R.raw.music)
        mediaPlayer.isLooping = false // If you want the music to loop
        mediaPlayer.start() // Start playing

        // Initialize views
        getStartedButton = findViewById(R.id.btn_get_started)
        getStartedButton.visibility = View.GONE // Hide button initially

        // Initialize image views (make sure these exist in your layout)
        imageViews.add(findViewById(R.id.image1))
        imageViews.add(findViewById(R.id.image2))
        imageViews.add(findViewById(R.id.image3))
        imageViews.add(findViewById(R.id.image4))
        imageViews.add(findViewById(R.id.image5))

        // Hide all images initially
        imageViews.forEach { it.visibility = View.INVISIBLE }

        // Start the animation sequence
        startImageAnimationSequence()
    }

    private fun startImageAnimationSequence() {
        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out)

        // Add decorative elements animations (stars, leaves, etc.)
        val starPulse = AnimationUtils.loadAnimation(this, R.anim.star_pulse)
        val leafFall = AnimationUtils.loadAnimation(this, R.anim.leaf_fall)

        // Apply animations sequentially
        for (i in imageViews.indices) {
            handler.postDelayed({
                val currentImage = imageViews[i]
                currentImage.visibility = View.VISIBLE

                // Apply different animations to different images
                when (i % 5) {
                    0 -> {
                        currentImage.startAnimation(fadeIn)
                        findViewById<ImageView>(R.id.leaf1).startAnimation(leafFall)
                    }
                    1 -> {
                        currentImage.startAnimation(fadeIn)
                    }
                    2 -> {
                        currentImage.startAnimation(fadeIn)
                        findViewById<ImageView>(R.id.leaf1).startAnimation(leafFall)
                    }
                    3 -> currentImage.startAnimation(fadeIn)
                    4 -> {
                        currentImage.startAnimation(fadeIn)
                        findViewById<ImageView>(R.id.leaf1).startAnimation(leafFall)
                    }
                }

                // Hide the image after showing it (except the last one)
                if (i < imageViews.size - 1) {
                    handler.postDelayed({
                        currentImage.startAnimation(fadeOut)
                        currentImage.visibility = View.INVISIBLE
                    }, animationDuration - 400)
                }
            }, i * animationDuration)
        }

        // After all animations, show the button
        handler.postDelayed({
            // Hide all images
            imageViews.forEach { it.visibility = View.GONE }

            // Show decorative elements if you want them to stay
            findViewById<ImageView>(R.id.star1).visibility = View.GONE
            findViewById<ImageView>(R.id.star2).visibility = View.GONE
            findViewById<ImageView>(R.id.leaf1).visibility = View.GONE

            // Show button with animation
            getStartedButton.visibility = View.VISIBLE
            getStartedButton.startAnimation(fadeIn)

            // Set button click listener
            getStartedButton.setOnClickListener {
                val intent = Intent(this, UserInfoActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fade_in, R.anim.fade_in)
            }
        }, totalAnimationTime)
    }

    override fun onDestroy() {
        // In onDestroy(), before super.onDestroy()
        if (::mediaPlayer.isInitialized) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
        super.onDestroy()
        // Remove any pending callbacks when activity is destroyed
        handler.removeCallbacksAndMessages(null)
    }
}