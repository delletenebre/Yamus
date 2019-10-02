package kg.delletenebre.yamus.ui.stations

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import kg.delletenebre.yamus.R


class StationsFragment : Fragment() {
    private lateinit var viewModel: StationsViewModel
    private lateinit var stationsPagerAdapter: StationsPagerAdapter
    private lateinit var stationPager: ViewPager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_stations, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar(view.findViewById(R.id.toolbar))

        stationsPagerAdapter = StationsPagerAdapter(view.context, childFragmentManager)
        stationPager = view.findViewById(R.id.stationPager)
        stationPager.adapter = stationsPagerAdapter

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        tabLayout.setupWithViewPager(stationPager)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(StationsViewModel::class.java)
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_profile -> {
                    findNavController().navigate(R.id.fragmentProfile)
                }
            }
            super.onOptionsItemSelected(it)
        }
    }
}