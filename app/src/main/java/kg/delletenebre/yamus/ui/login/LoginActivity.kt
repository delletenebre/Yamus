package kg.delletenebre.yamus.ui.login

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import kg.delletenebre.yamus.R

class LoginActivity : AppCompatActivity() {

    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        val username = findViewById<EditText>(R.id.username_edit_text)
        val password = findViewById<EditText>(R.id.password_edit_text)
        val loginButton = findViewById<Button>(R.id.login_button)
        val progressOverlay = findViewById<View>(R.id.progress_overlay)

        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        viewModel.loadingState.observe(this@LoginActivity, Observer { isLoading ->
            loginButton.isEnabled = !isLoading

            progressOverlay.visibility = if (isLoading) {
                View.VISIBLE
            } else {
                View.GONE
            }
        })

        viewModel.loginResult.observe(this@LoginActivity, Observer { loginResult ->
            Log.d("ahoha", "response: ${loginResult.message}")
            if (loginResult.isSuccess) {
                setResult(Activity.RESULT_OK)
                finish()
            } else {
                Snackbar.make(findViewById(R.id.container),
                        loginResult.message, Snackbar.LENGTH_LONG)
                        .withColor(ContextCompat.getColor(this, R.color.snackbarBackgroundError))
                        .show()
            }
        })

        loginButton.setOnClickListener {
            viewModel.login(username.text.toString(), password.text.toString())
        }
    }

    fun Snackbar.withColor(@ColorInt colorInt: Int): Snackbar{
        this.view.setBackgroundColor(colorInt)
        return this
    }
}