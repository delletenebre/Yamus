package kg.delletenebre.yamus.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.UserModel
import kg.delletenebre.yamus.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile,
                container,false)
        binding.userModel = UserModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar(view.findViewById(R.id.toolbar))
    }

    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_logout -> {
                    findNavController().navigate(R.id.fragmentHome)
                    UserModel.logout()
                }
            }
            super.onOptionsItemSelected(it)
        }
    }
}
