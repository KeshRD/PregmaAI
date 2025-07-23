package com.kairaxus.bundymom

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.kairaxus.bundymom.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityMainBinding
    private lateinit var backgroundAnimation: AnimationDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // View binding for cleaner code
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check if user is already logged in
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            goToHome()
            return
        }

        // Setup background gradient animation
        setupBackgroundAnimation()

        // Setup UI elements
        setupUI()
    }

    private fun setupBackgroundAnimation() {
        // Set gradient background
        val container = binding.root as LinearLayout
        container.background = ContextCompat.getDrawable(this, R.drawable.gradient_animation)
        backgroundAnimation = container.background as AnimationDrawable
        backgroundAnimation.setEnterFadeDuration(2000)
        backgroundAnimation.setExitFadeDuration(4000)
    }

    private fun setupUI() {
        // Load animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow)
        val bounce = AnimationUtils.loadAnimation(this, R.anim.bounce)

        // Animate logo
        binding.logoImageView.startAnimation(fadeIn)

        // Animate input fields
        binding.editTextEmail.startAnimation(fadeIn)
        binding.editTextPassword.startAnimation(fadeIn)

        // Setup login button with animation
        binding.loginButton.startAnimation(bounce)
        binding.loginButton.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                showErrorAnimation()
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show loading animation
            binding.progressBar.visibility = View.VISIBLE
            binding.loginButton.startAnimation(AnimationUtils.loadAnimation(this, R.anim.button_click))

            signInOrSignUp(email, password)
        }
    }

    private fun showErrorAnimation() {
        val shake = AnimationUtils.loadAnimation(this, R.anim.shake)
        binding.editTextEmail.startAnimation(shake)
        binding.editTextPassword.startAnimation(shake)
    }

    private fun signInOrSignUp(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                binding.progressBar.visibility = View.GONE

                if (task.isSuccessful) {
                    // Success animation
                    binding.logoImageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.success_pulse))
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    goToHome()
                } else {
                    // If login failed, try sign up
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this) { signUpTask ->
                            binding.progressBar.visibility = View.GONE

                            if (signUpTask.isSuccessful) {
                                // Success animation with different message
                                binding.logoImageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.success_pulse))
                                Toast.makeText(this, "Account created and logged in", Toast.LENGTH_SHORT).show()
                                goToHome()
                            } else {
                                showErrorAnimation()
                                Toast.makeText(this, "Authentication failed: ${signUpTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                Log.e("Auth", "Error", signUpTask.exception)
                            }
                        }
                }
            }
    }

    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    override fun onResume() {
        super.onResume()
        backgroundAnimation.start()
    }

    override fun onPause() {
        super.onPause()
        backgroundAnimation.stop()
    }
}