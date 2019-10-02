package kg.delletenebre.yamus

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kg.delletenebre.yamus.api.UserModel
import kg.delletenebre.yamus.ui.login.LoginActivity
import kg.delletenebre.yamus.utils.InjectorUtils
import kg.delletenebre.yamus.viewmodels.MainActivityViewModel


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainActivityViewModel
    private lateinit var navigationController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= 28){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_main)

        volumeControlStream = AudioManager.STREAM_MUSIC

        val bottomNavigationView: BottomNavigationView = findViewById(R.id.nav_view)
        navigationController = findNavController(R.id.fragmentContainer)
        bottomNavigationView.setupWithNavController(navigationController)

        viewModel = ViewModelProvider(this, InjectorUtils.provideMainActivityViewModel(this))
                .get(MainActivityViewModel::class.java)

        UserModel.token.observe(this, Observer {
            if (it.isEmpty()) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        })


//
//        viewModel.navigateToFragment.observe(this, Observer {
//            it?.getContentIfNotHandled()?.let { fragmentRequest ->
//                val transaction = supportFragmentManager.beginTransaction()
//                transaction.replace(
//                        R.id.fragmentContainer, fragmentRequest.fragment, fragmentRequest.tag)
//                if (fragmentRequest.backStack) transaction.addToBackStack(null)
//                transaction.commit()
//            }
//        })
//
//        viewModel.rootMediaId.observe(this,
//                Observer<String> { rootMediaId ->
//                    if (rootMediaId != null) {
//                        navigateToMediaItem(rootMediaId)
//                    }
//                })
//
//        viewModel.navigateToMediaItem.observe(this, Observer {
//            it?.getContentIfNotHandled()?.let { mediaId ->
//                navigateToMediaItem(mediaId)
//            }
//        })
    }

    override fun onResume() {
        super.onResume()

        if (!UserModel.isAuth()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == 1) {
//            if (resultCode != Activity.RESULT_OK) {
//                val intent = Intent(this, LoginActivity::class.java)
//                startActivityForResult(intent, 1)
//            } else {
//                GlobalScope.launch {
//                    YandexUser.updateUserTracks()
//                }
//                navigationController.navigate(R.id.fragmentProfile)
//                navigationController.popBackStack()
//            }
//        }
//    }

//    private fun navigateToMediaItem(mediaId: String) {
//        var fragment: MediaItemFragment? = getBrowseFragment(mediaId)
//        if (fragment == null) {
//            fragment = MediaItemFragment.newInstance(mediaId)
//            // If this is not the top level media (root), we add it to the fragment
//            // back stack, so that actionbar toggle and Back will work appropriately:
//            viewModel.showFragment(fragment, !isRootId(mediaId), mediaId)
//        }
//    }
//
//    private fun isRootId(mediaId: String) = mediaId == viewModel.rootMediaId.value
//
//    private fun getBrowseFragment(mediaId: String): MediaItemFragment? {
//        return supportFragmentManager.findFragmentByTag(mediaId) as MediaItemFragment?
//    }
}
