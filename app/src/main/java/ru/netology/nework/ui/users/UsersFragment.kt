package ru.netology.nework.ui.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.netology.nework.databinding.FragmentUsersBinding
import timber.log.Timber

@AndroidEntryPoint
class UsersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val usersViewModel by viewModels<UsersViewModel>()

        val binding = FragmentUsersBinding.inflate(inflater, container, false)

        val adapter = UsersAdapter { }
        binding.usersContainerRecycleView.adapter = adapter

        adapter.addLoadStateListener { loadState ->
            if (loadState.hasError) {
                Timber.i("INFO loadState.hasError")
                Toast.makeText(
                    context,
                    "Sorry, most probably INTERNET is OFF, check & swipe-refresh",
                    Toast.LENGTH_LONG
                ).show()
            }
            binding.swipeRefresh.isRefreshing = loadState.refresh is LoadState.Loading
        }

        binding.swipeRefresh.setOnRefreshListener {
            Timber.i("INFO SwipeRefresh event happens")
            adapter.refresh()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                usersViewModel.data.collectLatest { adapter.submitData(it) }
            }
        }

        return binding.root
    }
}