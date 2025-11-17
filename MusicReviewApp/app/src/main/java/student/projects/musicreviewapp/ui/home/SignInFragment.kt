package student.projects.musicreviewapp.ui.home

import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import student.projects.musicreviewapp.R
import student.projects.musicreviewapp.auth.SignInWithGoogle
import java.util.concurrent.Executor

class SignInFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private lateinit var googleSignInView: SignInWithGoogle

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        executor = ContextCompat.getMainExecutor(requireContext())

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    showToast("Authentication error: $errString")
                }
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    showToast("Authentication successful!")
                    navigateToHome()
                }
                override fun onAuthenticationFailed() {
                    showToast("Authentication failed")
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Login")
            .setSubtitle("Authenticate to access your MusicReviewApp account")
            .setDescription("Use your fingerprint to quickly sign in")
            .setNegativeButtonText("Cancel")
            .build()

        val backButton = view.findViewById<View>(R.id.back_button)
        val signInButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.sign_in_button)
        val biometricButton = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.biometric_button)
        val emailEditText = view.findViewById<TextInputEditText>(R.id.email_edit_text)
        val passwordEditText = view.findViewById<TextInputEditText>(R.id.password_edit_text)
        val signUpLink = view.findViewById<TextView>(R.id.sign_up_link)
        googleSignInView = view.findViewById(R.id.google_sign_in_view)

        backButton.setOnClickListener { findNavController().popBackStack() }
        signInButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            performSignIn(email, password)
        }

        biometricButton.setOnClickListener {
            if (BiometricManager.from(requireContext())
                    .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ==
                BiometricManager.BIOMETRIC_SUCCESS
            ) biometricPrompt.authenticate(promptInfo)
        }
        if (BiometricManager.from(requireContext())
                .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) !=
            BiometricManager.BIOMETRIC_SUCCESS
        ) biometricButton.visibility = View.GONE

        setupSignUpLink(signUpLink)
        googleSignInView.onGoogleSignInSuccess = { navigateToHome() }
        googleSignInView.onGoogleSignInFailure = { showToast(it) }
    }

    private fun setupSignUpLink(textView: TextView) {
        val fullText = "Don't have an account? Sign Up"
        val spannableString = SpannableString(fullText)
        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) { navigateToSignUp() }
            override fun updateDrawState(ds: TextPaint) {
                ds.color = resources.getColor(R.color.purple_link, null)
                ds.isUnderlineText = false
            }
        }
        val startIndex = fullText.indexOf("Sign Up")
        val endIndex = startIndex + "Sign Up".length
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun performSignIn(email: String, password: String) {
        if (email.isEmpty() || password.isEmpty()) {
            showToast("Please fill in all fields")
            return
        }
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) navigateToHome()
                else showToast(task.exception?.message ?: "Sign in failed")
            }
    }

    private fun navigateToSignUp() { findNavController().navigate(R.id.action_signInFragment_to_signUpFragment) }
    private fun navigateToHome() { findNavController().navigate(R.id.action_signInFragment_to_homeFragment) }
    private fun showToast(message: String) { android.widget.Toast.makeText(requireContext(), message, android.widget.Toast.LENGTH_SHORT).show() }
}
