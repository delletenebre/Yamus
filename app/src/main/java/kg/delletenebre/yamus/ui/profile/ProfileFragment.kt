package kg.delletenebre.yamus.ui.profile

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.YandexUser
import kg.delletenebre.yamus.databinding.FragmentProfileBinding
import java.text.SimpleDateFormat


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile,
                container,false)
        binding.yandexUser = YandexUser
        val subscriptionAvailableUntil = if (YandexUser.user.value!!.subscription.end.isEmpty()) {
            context!!.getString(R.string.profile_subscription_not_available)
        } else {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", App.instance.getLocale())
            val date = format.parse(YandexUser.user.value!!.subscription.end)!!
            val dateFormat = DateFormat.getLongDateFormat(context)
            val stringDate: String = dateFormat.format(date)
            context!!.getString(R.string.profile_subscription_until, stringDate)
        }
        binding.subscriptionAvailableUntil = subscriptionAvailableUntil
        Log.d("ahoha", "onCreateView")
        Log.d("ahoha", "user: ${YandexUser.user.value}")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(view.findViewById(R.id.toolbar))
        Log.d("ahoha", "onViewCreated")
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d("ahoha", "onActivityCreated")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ahoha", "onDestroy")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("ahoha", "onDestroyView")
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_logout -> {
                    findNavController().navigate(R.id.fragmentHome)
                    YandexUser.logout()
                }
            }
            super.onOptionsItemSelected(it)
        }
    }
}
