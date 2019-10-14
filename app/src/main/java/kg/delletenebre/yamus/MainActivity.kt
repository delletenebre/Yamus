package kg.delletenebre.yamus

import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kg.delletenebre.yamus.api.YandexUser
import kg.delletenebre.yamus.databinding.ActivityMainBinding
import kg.delletenebre.yamus.ui.login.LoginActivity
import kg.delletenebre.yamus.ui.settings.SettingsActivity
import kg.delletenebre.yamus.utils.InjectorUtils
import kg.delletenebre.yamus.viewmodels.MainActivityViewModel
import kg.delletenebre.yamus.viewmodels.NowPlayingViewModel


class MainActivity : AppCompatActivity() {
    private lateinit var navigationController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var nowPlayingViewModel: NowPlayingViewModel



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= 28) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        YandexUser.token.observe(this, Observer {
            if (it.isEmpty()) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        })

        viewModel = ViewModelProvider(this, InjectorUtils.provideMainActivityViewModel(this))
                .get(MainActivityViewModel::class.java)
        nowPlayingViewModel = ViewModelProvider(this, InjectorUtils.provideNowPlayingViewModel(this))
                .get(NowPlayingViewModel::class.java)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.nowPlayingModel = nowPlayingViewModel

//        setContentView(R.layout.activity_main)

        volumeControlStream = AudioManager.STREAM_MUSIC

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.nav_view)
        navigationController = findNavController(R.id.fragmentContainer)
        bottomNavigationView.setupWithNavController(navigationController)


        val bottomSheet = findViewById<LinearLayout>(R.id.bottomSheet)
        val sheetBehavior = BottomSheetBehavior.from(bottomSheet) as BottomSheetBehavior<LinearLayout>

        sheetBehavior.peekHeight = sheetBehavior.peekHeight * 2
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

//        val playerControlView = findViewById<PlayerControlView>(R.id.player_control_view)
//        CurrentPlaylist.player.observe(this, Observer {
//            Log.d("ahoha", "player= ${it}")
//            playerControlView.player = it
//        })


//        sheetBehavior.setBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
//            override fun onSlide(view: View, p1: Float) {
//
//            }
//
//            override fun onStateChanged(view: View, state: Int) {
//                when (state) {
//                    BottomSheetBehavior.STATE_HIDDEN -> {
//
//                    }
//
//                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
//
//                    }
//
//                    BottomSheetBehavior.STATE_EXPANDED -> {
//
//                    }
//
//                    BottomSheetBehavior.STATE_COLLAPSED -> {
//
//                    }
//
//                    BottomSheetBehavior.STATE_DRAGGING -> {
//                    }
//                    BottomSheetBehavior.STATE_SETTLING -> {
//                    }
//                }
//            }
//
//        })

    }

    override fun onResume() {
        super.onResume()

        if (!YandexUser.isAuth()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("value", "Permission Granted, Now you can use local drive .")
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .")
                }
            }
        }
    }

    private fun checkStoragePermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this@MainActivity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            with(AlertDialog.Builder(this)) {
                setCancelable(true)
                setTitle(this.context.getString(R.string.permission_write_external_storage_title))
                setMessage(this.context.getString(R.string.permission_write_external_storage_description))
                setPositiveButton(android.R.string.yes) { dialog, which ->
                    ActivityCompat.requestPermissions(this@MainActivity,
                            arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE)
                }
                create().show()
            }
        } else {
            ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }

    fun setupMainToolbar(toolbar: Toolbar) {
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_settings -> {
                    val intent = Intent(toolbar.context, SettingsActivity::class.java)
                    startActivity(intent)
                }
                R.id.action_profile -> {
                    navigationController.navigate(R.id.fragmentProfile)
                }
            }
            super.onOptionsItemSelected(it)
        }
    }

    companion object {
        const val PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1
    }

}
