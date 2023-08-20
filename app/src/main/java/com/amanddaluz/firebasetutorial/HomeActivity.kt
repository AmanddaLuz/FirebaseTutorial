package com.amanddaluz.firebasetutorial

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.amanddaluz.firebasetutorial.databinding.ActivityHomeBinding
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics

enum class ProviderType{
    BASIC,
    GOOGLE,
    FACEBOOK
}

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Setup
        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")
        setup(email ?: "", provider ?: "")

        //Guardando Dados
        val prefs = getSharedPreferences(
            "com.amanddaluz.firebasetutorial.PREFERENCE_FILE_KEY",
            Context.MODE_PRIVATE).edit()

        prefs.putString("email", email)
        prefs.putString("provider", provider)
        prefs.apply()
    }

    private fun setup(email: String, provider: String) = with(binding) {
        title = "Início"

        edtUserEmail.text = email
        edtUserProvedor.text = provider

        btnLogout.setOnClickListener {

            //Apagando Dados
            val prefs = getSharedPreferences(
                "com.amanddaluz.firebasetutorial.PREFERENCE_FILE_KEY",
                Context.MODE_PRIVATE).edit()

            prefs.clear()
            prefs.apply()

            if (provider == ProviderType.FACEBOOK.name){
                LoginManager.getInstance().logOut()
            }

            FirebaseAuth.getInstance().signOut()
            onBackPressed()
        }

        btnError.setOnClickListener {

            //Enviando informações adicionais
            FirebaseCrashlytics.getInstance().setUserId(email)
            FirebaseCrashlytics.getInstance().setCustomKey("provider", provider)

            //Enviando log de contexto
            FirebaseCrashlytics.getInstance().log("Click no botão de forçar erro!")
            //Forçando erro
            throw RuntimeException("Erro forçado!")
        }
    }


}