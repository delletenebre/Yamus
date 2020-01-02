package kg.delletenebre.yamus

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.MatrixCursor
import android.media.AudioManager
import android.os.Bundle
import android.provider.BaseColumns
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.cursoradapter.widget.CursorAdapter
import androidx.cursoradapter.widget.SimpleCursorAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
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
import kg.delletenebre.yamus.ui.search.SearchViewModel
import kg.delletenebre.yamus.ui.settings.SettingsActivity
import kg.delletenebre.yamus.utils.InjectorUtils
import kg.delletenebre.yamus.utils.UI
import kg.delletenebre.yamus.viewmodels.MainActivityViewModel
import kotlinx.coroutines.launch
import kotlin.system.exitProcess


class MainActivity : ScopedAppActivity() {
    private val PERMISSION_WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1

    private lateinit var navigationController: NavController
    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainActivityViewModel
    private var searchViewModel: SearchViewModel? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT >= 28) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        viewModel = ViewModelProvider(this, InjectorUtils.provideMainActivityViewModel(this)).get()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.currentPlaylist = CurrentPlaylist

        CurrentPlaylist.currentTrack.observe(this, Observer {
            if (it == null) {

            } else {

            }
        })

        volumeControlStream = AudioManager.STREAM_MUSIC

        navigationController = findNavController(R.id.fragmentContainer)
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.nav_view)
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

        requestStoragePermission()

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

    fun setupMainToolbar(toolbar: Toolbar, searchQuery: String? = null) {
        initSearch(toolbar.menu, searchQuery)

        UI.setMenuIconsColor(this, toolbar.menu)
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

    private fun initSearch(menu: Menu, searchQuery: String? = null) {
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager

        val from = arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
        val to = intArrayOf(R.id.item_label)
        val cursorAdapter = SimpleCursorAdapter(
                this,
                R.layout.search_item,
                null,
                from,
                to,
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER
        )
        searchView.suggestionsAdapter = cursorAdapter
        searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                hideKeyboard(searchView)

                if (navigationController.currentDestination?.id != R.id.fragmentSearch) {
                    val bundle = bundleOf("searchQuery" to query)
                    navigationController.navigate(R.id.fragmentSearch, bundle)
                } else {
                    searchViewModel?.search(query ?: "")
                }

                return false
            }

            override fun onQueryTextChange(query: String?): Boolean {
                launch {
                    val cursor = MatrixCursor(arrayOf(BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1))
                    query?.let {
                        if (query.length > 2) {
                            YaApi.searchSuggest(it).forEachIndexed { index, suggestion ->
                                cursor.addRow(arrayOf(index, suggestion))
                            }
                        }
                    }
                    cursorAdapter.changeCursor(cursor)
                    cursorAdapter.notifyDataSetChanged()
                }
                return true
            }
        })

        searchView.setOnSuggestionListener(object: SearchView.OnSuggestionListener {
            override fun onSuggestionSelect(position: Int): Boolean {
                return false
            }

            override fun onSuggestionClick(position: Int): Boolean {
                hideKeyboard(searchView)
                val cursor = searchView.suggestionsAdapter.getItem(position) as Cursor
                val selection = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1))
                searchView.setQuery(selection, false)

                // Do something with selection
                return true
            }
        })

        searchView.apply {
            setSearchableInfo(searchManager.getSearchableInfo(componentName))
            val textView = findViewById<AutoCompleteTextView>(R.id.search_src_text)
            textView.threshold = 3
            if (searchQuery != null) {
                searchItem.expandActionView()
                searchView.setQuery(searchQuery, true)
                searchView.clearFocus()
            }

        }
    }

    fun setSearchViewModel(viewModel: SearchViewModel?) {
        searchViewModel = viewModel
    }

    fun Context.hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun Fragment.hideKeyboard() {
        view?.let {
            activity?.hideKeyboard(it)
        }
    }
}
