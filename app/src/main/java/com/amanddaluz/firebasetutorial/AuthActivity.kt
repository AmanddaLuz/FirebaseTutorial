package com.amanddaluz.firebasetutorial

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.amanddaluz.firebasetutorial.databinding.ActivityAuthBinding
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.iid.FirebaseInstanceIdReceiver;


class AuthActivity : AppCompatActivity() {
    private val GOOGLE_SIGN_IN = 100
    private val callbackManager = CallbackManager.Factory.create()

    private lateinit var binding: ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        //Splash
        /*Thread.sleep(2000)
        setTheme(com.google.android.material.R.style.AlertDialog_AppCompat)
*/
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Analytics Event
        val analytics: FirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        val bundle = Bundle()
        bundle.putString("message", "A inicialização do firebase está completa!")
        analytics.logEvent("InitScreen", bundle)

        //Setup
        setup()
        session()
    }

    override fun onStart() {
        super.onStart()
        binding.btnGoogle.visibility = View.VISIBLE
    }

    private fun session() {
        val prefs = getSharedPreferences(
            "com.amanddaluz.firebasetutorial.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE
        )

        val email = prefs.getString("email", null)
        val provider = prefs.getString("provider", null)

        if (email != null && provider != null) {
            binding.btnGoogle.visibility = View.INVISIBLE
            showHome(email, ProviderType.valueOf(provider))
        }
    }

    private fun setup() = with(binding) {
        title = "Autnehtication"
        btnRegister.setOnClickListener {
            if (edtUserEmail.text?.isNotEmpty() == true && edtUserPassword.text?.isNotEmpty() == true) {
                print(edtUserEmail.text.toString())
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(
                        edtUserEmail.text.toString(),
                        edtUserPassword.text.toString()
                    )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            showHome(
                                it.result.user?.email.toString(),
                                ProviderType.BASIC
                            )
                        } else {
                            showAlert()
                        }
                    }
            }
        }

        btnLogin.setOnClickListener {
            if (edtUserEmail.text?.isNotEmpty() == true && edtUserPassword.text?.isNotEmpty() == true) {
                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(
                        edtUserEmail.text.toString(),
                        edtUserPassword.text.toString()
                    )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            showHome(
                                it.result?.user?.email ?: "",
                                ProviderType.BASIC
                            )
                        } else {
                            showAlert()
                        }
                    }
            }
        }

        btnGoogle.setOnClickListener {
            // Configurando autenticação google
            val googleSignInOptions = GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
            ).requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val googleClient = GoogleSignIn.getClient(this@AuthActivity, googleSignInOptions)
            googleClient.signOut()

            startActivityForResult(googleClient.signInIntent, GOOGLE_SIGN_IN)
        }

        btnFacebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this@AuthActivity, listOf("email"))
            LoginManager.getInstance().registerCallback(callbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult) {

                        result.let {
                            val token = it.accessToken

                            val credential = FacebookAuthProvider.getCredential(token.token)
                            FirebaseAuth.getInstance().signInWithCredential(credential)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        showHome(
                                            it.result.user?.email ?: "", ProviderType.FACEBOOK
                                        )
                                    } else {
                                        showAlert()
                                    }
                                }
                        }
                    }

                    override fun onCancel() {

                    }

                    override fun onError(error: FacebookException) {
                        showAlert()
                    }
                })
        }
    }

    private fun showAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Error")
        builder.setMessage("Erro ao autenticar usuário!")
        builder.setPositiveButton("Aceitar", null)
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun showHome(email: String, provider: ProviderType) {
        val homeIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra("email", email)
            putExtra("provider", provider.name)
        }
        startActivity(homeIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {

                    val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                showHome(
                                    account.email ?: "", ProviderType.GOOGLE
                                )
                            } else {
                                showAlert()
                            }
                        }
                }
            } catch (e: ApiException) {
                showAlert()
            }
        }
    }
}