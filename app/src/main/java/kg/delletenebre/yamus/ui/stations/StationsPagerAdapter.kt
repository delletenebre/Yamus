package kg.delletenebre.yamus.ui.stations

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.ui.stations.categories.CategoryPageFragment
import kg.delletenebre.yamus.ui.stations.categories.CategoryTab

class StationsPagerAdapter(context: Context, fragmentManager: FragmentManager)
    : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    private val tabs = listOf(
            CategoryTab(context.getString(R.string.stations_tab_recommended), "recommended"),
            CategoryTab(context.getString(R.string.stations_tab_activity), "activity"),
            CategoryTab(context.getString(R.string.stations_tab_mood), "mood"),
            CategoryTab(context.getString(R.string.stations_tab_genre), "genre"),
            CategoryTab(context.getString(R.string.stations_tab_era), "epoch"),
            CategoryTab(context.getString(R.string.stations_tab_other), listOf("local", "author"))

    )

    override fun getCount(): Int = tabs.size

    override fun getItem(position: Int): Fragment {
        val fragment = CategoryPageFragment(tabs[position])
        fragment.arguments = Bundle().apply {
            putInt("test", position)
        }
        return fragment
    }

    override fun getPageTitle(position: Int): CharSequence {
        return tabs[position].title
    }
}