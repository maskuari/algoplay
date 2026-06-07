package com.algoplay.app

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var loginScroll: ScrollView
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnTogglePassword: ImageButton
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleLogin: Button
    private lateinit var btnGuestLogin: Button
    private lateinit var txtGoRegister: TextView
    private lateinit var progressBar: ProgressBar
    private var isPasswordVisible = false

    private val googleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)

            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken

                if (idToken != null) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    showLoading(false)
                    Toast.makeText(this, "Token Google kosong. Cek SHA-1 Firebase.", Toast.LENGTH_LONG).show()
                }
            } catch (e: ApiException) {
                showLoading(false)
                Toast.makeText(this, "Login Google gagal. Kode: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupGoogleLogin()

        loginScroll = findViewById(R.id.loginScroll)
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnTogglePassword = findViewById(R.id.btnTogglePassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        btnGuestLogin = findViewById(R.id.btnGuestLogin)
        txtGoRegister = findViewById(R.id.txtGoRegister)
        progressBar = findViewById(R.id.progressBar)

        edtEmail.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) scrollToBottom()
        }

        edtPassword.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) scrollToBottom()
        }

        btnLogin.setOnClickListener {
            loginUser()
        }
        btnLogin.enableTapFeedback()

        btnGoogleLogin.setOnClickListener {
            signInWithGoogle()
        }
        btnGoogleLogin.enableTapFeedback()

        btnGuestLogin.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(EXTRA_GUEST_MODE, true)
            startActivity(intent)
            finish()
        }
        btnGuestLogin.enableTapFeedback()

        btnTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }
        btnTogglePassword.enableTapFeedback()

        txtGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        txtGoRegister.enableTapFeedback()
    }

    private fun setupGoogleLogin() {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private fun signInWithGoogle() {
        showLoading(true)

        googleSignInClient.signOut().addOnCompleteListener {
            googleLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveGoogleUserToFirestore()
                } else {
                    showLoading(false)
                    Toast.makeText(
                        this,
                        "Firebase Google Auth gagal: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun saveGoogleUserToFirestore() {
        val user = auth.currentUser

        if (user == null) {
            showLoading(false)
            Toast.makeText(this, "User Google tidak ditemukan", Toast.LENGTH_LONG).show()
            return
        }

        val userData = hashMapOf(
            "uid" to user.uid,
            "name" to (user.displayName ?: "Teman"),
            "email" to (user.email ?: ""),
            "level" to 1,
            "stars" to 0,
            "totalScoreLeaderboard" to 0,
            "loginProvider" to "google",
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("users")
            .document(user.uid)
            .set(userData, SetOptions.merge())
            .addOnSuccessListener {
                showLoading(false)
                Toast.makeText(this, "Login Google berhasil", Toast.LENGTH_SHORT).show()

                openAfterLogin(user.uid)
                finish()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(
                    this,
                    "Login berhasil, tapi data gagal disimpan: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun scrollToBottom() {
        loginScroll.postDelayed({
            loginScroll.smoothScrollTo(0, loginScroll.bottom)
        }, 250)
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible

        if (isPasswordVisible) {
            edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
            btnTogglePassword.setImageResource(R.drawable.ic_eye_off)
            btnTogglePassword.contentDescription = "Sembunyikan password"
        } else {
            edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
            btnTogglePassword.setImageResource(R.drawable.ic_eye)
            btnTogglePassword.contentDescription = "Tampilkan password"
        }

        edtPassword.setSelection(edtPassword.text?.length ?: 0)
    }

    private fun loginUser() {
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString().trim()

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

        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_SHORT).show()

                    openAfterLogin(auth.currentUser?.uid)
                    finish()
                } else {
                    Toast.makeText(
                        this,
                        "Login gagal: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
        btnGoogleLogin.isEnabled = !isLoading
        btnGuestLogin.isEnabled = !isLoading
        btnTogglePassword.isEnabled = !isLoading
        txtGoRegister.isEnabled = !isLoading
    }

    private fun openAfterLogin(uid: String?) {
        uid?.let { ProgressBridge.mergePendingGuestProgress(this, it) }
        val hasSeenOnboarding = uid != null &&
            getSharedPreferences(PREF_ONBOARDING, MODE_PRIVATE)
                .getBoolean("${KEY_ONBOARDING_PREFIX}$uid", false)
        val nextScreen = if (hasSeenOnboarding) MainActivity::class.java else OnboardingActivity::class.java
        val intent = Intent(this, nextScreen)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    companion object {
        const val EXTRA_GUEST_MODE = "extra_guest_mode"
        private const val PREF_ONBOARDING = "algoplay_onboarding"
        private const val KEY_ONBOARDING_PREFIX = "seen_"
    }
}
