package com.algoplay.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var registerScroll: ScrollView
    private lateinit var edtName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var txtGoLogin: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        registerScroll = findViewById(R.id.registerScroll)
        edtName = findViewById(R.id.edtName)
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnRegister = findViewById(R.id.btnRegister)
        txtGoLogin = findViewById(R.id.txtGoLogin)
        progressBar = findViewById(R.id.progressBar)

        edtName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) scrollToBottom()
        }

        edtEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) scrollToBottom()
        }

        edtPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) scrollToBottom()
        }

        btnRegister.setOnClickListener {
            registerUser()
        }
        btnRegister.enableTapFeedback()

        txtGoLogin.setOnClickListener {
            finish()
        }
        txtGoLogin.enableTapFeedback()
    }

    private fun scrollToBottom() {
        registerScroll.postDelayed({
            registerScroll.smoothScrollTo(0, registerScroll.bottom)
        }, 250)
    }

    private fun registerUser() {
        val name = edtName.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString().trim()

        if (name.isEmpty()) {
            edtName.error = "Nama wajib diisi"
            edtName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            edtEmail.error = "Email wajib diisi"
            edtEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            edtPassword.error = "Password wajib diisi"
            edtPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            edtPassword.error = "Password minimal 6 karakter"
            edtPassword.requestFocus()
            return
        }

        showLoading(true)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid

                    if (userId != null) {
                        saveUserToFirestore(userId, name, email)
                    } else {
                        showLoading(false)
                        Toast.makeText(this, "Gagal mengambil UID user", Toast.LENGTH_LONG).show()
                    }
                } else {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Register gagal: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveUserToFirestore(userId: String, name: String, email: String) {
        val userData = hashMapOf(
            "uid" to userId,
            "name" to name,
            "email" to email,
            "level" to 1,
            "stars" to 0,
            "totalScoreLeaderboard" to 0,
            "loginProvider" to "email",
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Akun berhasil dibuat", Toast.LENGTH_SHORT).show()
                ProgressBridge.mergePendingGuestProgress(this, userId)

                // Arahkan ke OnboardingActivity setelah sukses daftar
                val intent = Intent(this, OnboardingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Akun dibuat, tapi data gagal disimpan: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !isLoading
        txtGoLogin.isEnabled = !isLoading
    }
}
