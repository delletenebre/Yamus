package kg.delletenebre.yamus.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.YandexCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupToolbar(findViewById(R.id.toolbar))
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }

        override fun onBindPreferences() {
            super.onBindPreferences()

            val context = context!!


            val clearCache = findPreference<Preference>("clear_cache")
            clearCache?.summary = context.getString(R.string.pref_summary_clear_cache, YandexCache.downloadedTracksCount(), YandexCache.downloadedTracksSize())
            clearCache?.setOnPreferenceClickListener {
                with(AlertDialog.Builder(context)) {
                    setCancelable(true)
                    setTitle(context.getString(R.string.pref_title_dialog_clear_cache))
                    setMessage(context.getString(R.string.pref_summary_dialog_clear_cache))
                    setPositiveButton(android.R.string.yes) { _, _ ->
                        GlobalScope.launch(Dispatchers.Main) {
                            clearCache.summary = context.getString(R.string.please_wait)
                            withContext(Dispatchers.IO) {
                                YandexCache.clear(context)
                            }
                            clearCache.summary = context.getString(R.string.pref_summary_cache_cleared)
                        }
                    }
                    setNegativeButton(android.R.string.cancel) { dialog, _ ->
                        dialog.cancel()
                    }
                    create().show()
                }
                true
            }
        }

    }
}