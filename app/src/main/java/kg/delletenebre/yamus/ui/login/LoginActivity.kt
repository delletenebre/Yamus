package kg.delletenebre.yamus.ui.login

import android.content.Intent
import android.os.Bundle
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kg.delletenebre.yamus.MainActivity
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        setupToolbar(findViewById(R.id.toolbar))

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.loginResult.observe(this@LoginActivity, Observer {
            if (it.isSuccess) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                Snackbar.make(findViewById(R.id.container),
                        it.message, Snackbar.LENGTH_LONG)
                        .withColor(ContextCompat.getColor(this, R.color.snackbarBackgroundError))
                        .show()
            }
        })
//
//        viewModel.loginResult.observe(this@LoginActivity, Observer { loginResult ->
//            Log.d("ahoha", "response: ${loginResult.message}")
//            if (loginResult.isSuccess) {
//                setResult(Activity.RESULT_OK)
//                finish()
//            } else {
//                Snackbar.make(findViewById(R.id.container),
//                        loginResult.message, Snackbar.LENGTH_LONG)
//                        .withColor(ContextCompat.getColor(this, R.color.snackbarBackgroundError))
//                        .show()
//            }
//        })
//
//        loginButton.setOnClickListener {
//            viewModel.login(username.text.toString(), password.text.toString())
//        }
    }

    private fun setupToolbar(toolbar: Toolbar) {
    }

    fun Snackbar.withColor(@ColorInt colorInt: Int): Snackbar{
        this.view.setBackgroundColor(colorInt)
        return this
    }
}