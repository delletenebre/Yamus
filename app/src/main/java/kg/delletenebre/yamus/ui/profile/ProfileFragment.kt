package kg.delletenebre.yamus.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.fragment.findNavController
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.YaApi
import kg.delletenebre.yamus.databinding.ProfileFragmentBinding
import kg.delletenebre.yamus.ui.login.LoginActivity


class ProfileFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val root = ProfileFragmentBinding.inflate(inflater, container, false).also {
            it.lifecycleOwner = this
            it.viewModel = viewModel
            it.executePendingBindings()
        }.root

        setupToolbar(root.findViewById(R.id.toolbar))

        return root
    }


    private fun setupToolbar(toolbar: Toolbar) {
        toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
        toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_logout -> {
                    YaApi.logout()
                    startActivity(Intent(activity, LoginActivity::class.java))
                    activity?.finish()
                }
            }
            super.onOptionsItemSelected(it)
        }
    }
}
