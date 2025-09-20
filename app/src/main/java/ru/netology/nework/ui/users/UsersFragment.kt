package ru.netology.nework.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.databinding.FragmentUsersBinding

@AndroidEntryPoint
class UsersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val usersViewModel = ViewModelProvider(this)[UsersViewModel::class.java]

        val binding = FragmentUsersBinding.inflate(inflater, container, false)

        val adapter = UsersAdapter { user ->

        }

        binding.usersContainerRecycleView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                usersViewModel.data.collectLatest { adapter.submitData(it) }
            }
        }

        return binding.root
    }
}