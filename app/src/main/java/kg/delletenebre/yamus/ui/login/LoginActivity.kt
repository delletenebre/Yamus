package kg.delletenebre.yamus.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kg.delletenebre.yamus.api.YandexApi
import kg.delletenebre.yamus.databinding.ActivityLoginBinding


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, YandexApi.oauthUri))
        }
    }
}