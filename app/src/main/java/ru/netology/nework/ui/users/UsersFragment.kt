package ru.netology.nework.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
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

        usersViewModel.text.observe(viewLifecycleOwner) {
            binding.textNotifications.text = it
        }
        return binding.root
    }
}