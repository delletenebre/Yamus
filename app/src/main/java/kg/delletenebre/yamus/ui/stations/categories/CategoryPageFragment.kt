package kg.delletenebre.yamus.ui.stations.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.response.Station
import kg.delletenebre.yamus.ui.stations.StationsViewModel
import kg.delletenebre.yamus.utils.Converter
import kg.delletenebre.yamus.utils.InjectorUtils
import kg.delletenebre.yamus.viewmodels.MainActivityViewModel
import kg.delletenebre.yamus.views.GridSpacingItemDecoration

class CategoryPageFragment(private val category: CategoryTab) : Fragment() {
    private lateinit var mainViewModel: MainActivityViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.station_category_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = activity ?: return

        mainViewModel = ViewModelProvider(context, InjectorUtils.provideMainActivityViewModel(context))
                .get(MainActivityViewModel::class.java)

        val viewModel = ViewModelProvider(parentFragment!!).get(StationsViewModel::class.java)
        val stationsContainer = view.findViewById<RecyclerView>(R.id.stationsContainer)
        val spacing = Converter.dp2px(16, activity!!)
        val spanCount = (stationsContainer.layoutManager as GridLayoutManager).spanCount
        stationsContainer.addItemDecoration(GridSpacingItemDecoration(spanCount, spacing, true))

        val stationsAdapter = CategoriesAdapter(object: CategoriesAdapter.ItemListener {
            override fun onClick(item: Station, position: Int) {
                mainViewModel.stationClicked(item)
                //YandexMusic.getStationTracks(item.data.id.type, item.data.id.tag)
            }
        })
        stationsContainer.adapter = stationsAdapter

        if (category.tags.contains("recommended")) {
            viewModel.recommendedStations.observe(this, Observer {stations ->
                stationsAdapter.items = stations
                stationsAdapter.notifyDataSetChanged()
            })
        } else {
            viewModel.stations.observe(this, Observer {stations ->
                val filteredStations = stations.filter {
                    category.tags.contains(it.data.id.type)
                }
                stationsAdapter.items = filteredStations
//                stationsAdapter.items.clear()
//                stationsAdapter.items.addAll(filteredStations)
                stationsAdapter.notifyDataSetChanged()
            })
        }
    }
}