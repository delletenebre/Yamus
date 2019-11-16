package kg.delletenebre.yamus

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.billy.android.swipe.SmartSwipe
import com.billy.android.swipe.SmartSwipeWrapper
import com.billy.android.swipe.SwipeConsumer
import com.billy.android.swipe.consumer.StayConsumer
import com.billy.android.swipe.listener.SimpleSwipeListener
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.analytics.FirebaseAnalytics
import kg.delletenebre.yamus.api.YaApi
import kg.delletenebre.yamus.databinding.ActivityMainBinding
import kg.delletenebre.yamus.media.actions.CustomActionsHelper
import kg.delletenebre.yamus.media.library.CurrentPlaylist
import kg.delletenebre.yamus.ui.login.LoginActivity
import kg.delletenebre.yamus.ui.settings.SettingsActivity
import kg.delletenebre.yamus.utils.InjectorUtils
import kg.delletenebre.yamus.viewmodels.MainActivityViewModel
import kg.delletenebre.yamus.viewmodels.NowPlayingViewModel
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    private lateinit var navigationController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var nowPlayingViewModel: NowPlayingViewModel
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= 28) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

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

        SmartSwipe.wrap(findViewById<View>(R.id.nowPlayingTrackTitle))
                .addConsumer(StayConsumer()) //contentView stay while swiping with StayConsumer
                .enableHorizontal()
                .addListener(object : SimpleSwipeListener() {
                    override fun onSwipeOpened(wrapper: SmartSwipeWrapper?, consumer: SwipeConsumer?, direction: Int) {
                        Log.d("ahoha", "direction: $direction")
                        when (direction) {
                            SwipeConsumer.DIRECTION_LEFT -> {
                                val player = CurrentPlaylist.player.value
                                if (player != null) {
                                    CustomActionsHelper.next(player)
                                }
                            }
                            SwipeConsumer.DIRECTION_RIGHT -> {
                                val player = CurrentPlaylist.player.value
                                if (player != null) {
                                    CustomActionsHelper.previous(player)
                                }
                            }
                        }
                    }
                })

        val playerControlView = findViewById<PlayerControlView>(R.id.nowPlayingPlayerControl)
        playerControlView.showTimeoutMs = -1
        CurrentPlaylist.player.observe(this, Observer {
            playerControlView.player = it
        })


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

        if (!YaApi.isAuth()) {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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
                R.id.action_close_app -> {
                    this.finish()
                    exitProcess(0)
                }
            }
            super.onOptionsItemSelected(it)
        }
    }
}
