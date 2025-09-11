package ru.netology.nework.ui.posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.ui.auth.AuthApp
import ru.netology.nework.ui.auth.AuthStateViewModel
import javax.inject.Inject

@AndroidEntryPoint
class PostsFragment : Fragment() {

    @Inject
    lateinit var authApp: AuthApp

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val postsViewModel = ViewModelProvider(this)[PostsViewModel::class.java]

        val binding = FragmentPostsBinding.inflate(inflater, container, false)

        postsViewModel.text.observe(viewLifecycleOwner) {
            binding.textHome.text = it
        }

        activity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

                val authStateViewModel: AuthStateViewModel by viewModels()

                menuInflater.inflate(R.menu.home_dots_menu, menu)
                println("INFO toolbarMain's menu is inflated")

                authStateViewModel.data.flowWithLifecycle(lifecycle).onEach {
                    menu.setGroupVisible(R.id.unauthenticated, !authStateViewModel.authenticated)
                    menu.setGroupVisible(R.id.authenticated, authStateViewModel.authenticated)
//                    adapter.refresh()
                }.launchIn(lifecycleScope)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean =
                when (menuItem.itemId) {
                    R.id.signin -> {
                        // TODO: just hardcode it, implementation must be in homework
                        findNavController().navigate(R.id.action_navigation_home_to_navigation_auth)
                        true
                    }

                    R.id.signme -> {
                        // TODO: just hardcode it, implementation must be in homework
                        authApp.setAuth(5, "x-token")
                        true
                    }

                    R.id.signout -> {
                        authApp.removeAuth()
                        true
                    }

                    else -> false
                }

        }, viewLifecycleOwner)


        return binding.root
    }
}