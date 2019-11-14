package kg.delletenebre.yamus.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kg.delletenebre.yamus.MainActivity
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.YaApi
import kg.delletenebre.yamus.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.loginResult.observe(this@LoginActivity, Observer {
            if (it == 200) {
                if (YaApi.isAuth()) {
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            } else {
                val message = if (it == 400) {
                    getString(R.string.auth_error_wrong_username_or_password)
                } else {
                    getString(R.string.auth_error_server_error, it)
                }
                Snackbar.make(findViewById(R.id.container),
                        message, Snackbar.LENGTH_LONG)
                        .withColor(ContextCompat.getColor(this, R.color.snackbarBackgroundError))
                        .show()
            }
        })
    }

    fun Snackbar.withColor(@ColorInt colorInt: Int): Snackbar{
        this.view.setBackgroundColor(colorInt)
        return this
    }
}