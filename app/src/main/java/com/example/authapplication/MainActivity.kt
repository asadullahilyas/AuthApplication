package com.example.authapplication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpLoginViews()
        setUpSignUpViews()
    }

    private fun setUpLoginViews() {
        val loginEmailView = findViewById<EditText>(R.id.loginEmailView)
        val loginPasswordView = findViewById<EditText>(R.id.loginPasswordView)
        val loginButton = findViewById<View>(R.id.loginButton)
        val loginProgressView = findViewById<View>(R.id.loginProgressView)

        loginButton.setOnClickListener {

            val email = loginEmailView.text.toString().trim()
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Invalid email")
                return@setOnClickListener
            }

            val password = loginPasswordView.text.toString().trim()
            if (password.isEmpty() || password.isBlank()) {
                showToast("Password is empty")
                return@setOnClickListener
            }

            if (password.length < 8) {
                showToast("Password should be at least 8 characters long")
                return@setOnClickListener
            }

            loginProgressView.visibility = View.VISIBLE

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {

                    loginProgressView.visibility = View.GONE

                    if (it.isSuccessful) {
                        val intent = Intent(this@MainActivity, UserListActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        showToast(it.exception?.message ?: "Unknown error occurred")
                    }
                }
        }
    }

    private fun setUpSignUpViews() {
        val signUpEmailView = findViewById<EditText>(R.id.signUpEmailView)
        val signUpPasswordView = findViewById<EditText>(R.id.signUpPasswordView)
        val signUpConfirmPasswordView = findViewById<EditText>(R.id.signUpConfirmPasswordView)
        val signUpButton = findViewById<View>(R.id.signUpButton)
        val signUpProgressView = findViewById<View>(R.id.signUpProgressView)

        signUpButton.setOnClickListener {

            val email = signUpEmailView.text.toString().trim()
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showToast("Invalid email")
                return@setOnClickListener
            }

            val password = signUpPasswordView.text.toString().trim()
            if (password.isEmpty() || password.isBlank()) {
                showToast("Password is empty")
                return@setOnClickListener
            }

            if (password.length < 8) {
                showToast("Password should be at least 8 characters long")
                return@setOnClickListener
            }

            val confirmPassword = signUpConfirmPasswordView.text.toString().trim()
            if (confirmPassword != password) {
                showToast("Both passwords must match")
                return@setOnClickListener
            }

            signUpProgressView.visibility = View.VISIBLE

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {

                signUpProgressView.visibility = View.GONE

                if (it.isSuccessful) {
                    showToast("Your account has been created. Please login")
                    signUpEmailView.setText("")
                    signUpPasswordView.setText("")
                    signUpConfirmPasswordView.setText("")
                } else {
                    showToast(it.exception?.message ?: "Unknown error occurred")
                }
            }
        }
    }
}

fun AppCompatActivity.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}