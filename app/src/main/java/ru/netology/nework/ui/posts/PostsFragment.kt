package ru.netology.nework.ui.posts

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentPostsBinding
import ru.netology.nework.repository.PostRepositoryOnServer
import ru.netology.nework.ui.auth.AuthApp
import ru.netology.nework.ui.auth.AuthStateViewModel
import java.io.IOException
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

        val authAppStateFlowAsLifeData = authApp.authStateFlow.asLiveData()

        val binding = FragmentPostsBinding.inflate(inflater, container, false)

        val adapter = PostsAdapter({ post, key ->
            when (key) {
                KeyPostViewHolder.VIDEO -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.video))
                    startActivity(intent)
                }

                KeyPostViewHolder.LIKE -> {
                    if (authApp.authenticated) {
                        postsViewModel.likeVM(post.id)
                    } else throw IOException(
                        "ERROR: KeyPostViewHolder.LIKE - unauthorized user likes a post"
                    )
                }

                KeyPostViewHolder.SHARE -> {
                    val intent = Intent().apply {
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plain"
                        action = Intent.ACTION_SEND
                    }
                    val shareIntent = Intent.createChooser(intent, "Sharing the post")
                    startActivity(shareIntent)
                    postsViewModel.shareVM(post.id)
                }

                KeyPostViewHolder.POST -> {
//                    findNavController().navigate(
//                        R.id.action_mainFragment_to_focusFragment,
//                        Bundle().apply { this.putString("TEXT_TRANSFER", post.id.toString()) })
                }

                KeyPostViewHolder.REMOVE -> postsViewModel.removeVM(post.id)
                KeyPostViewHolder.EDIT -> {
//                    findNavController().navigate(
//                        R.id.action_mainFragment_to_editorFragment,
//                        Bundle().apply { this.putString("TEXT_TRANSFER", post.id.toString()) })
//                    postsViewModel.editVM(post)
                }

                KeyPostViewHolder.CANCEL -> postsViewModel.cancelEditVM()
            }
        }, authApp)

        binding.postsContainerRecycleView.adapter = adapter
        binding.plusButton.isVisible = authApp.authenticated

        binding.plusButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_postsEditorFragment)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                postsViewModel.data.collectLatest { adapter.submitData(it) }
            }
        }

        authAppStateFlowAsLifeData.observe(viewLifecycleOwner) {
            binding.plusButton.isVisible = authApp.authenticated
            adapter.refresh()
        }

        postsViewModel.dbFlag.observe(viewLifecycleOwner) {
            when (it) {
                PostRepositoryOnServer.DbFlagsList.REFRESH_REQUEST -> adapter.refresh()
                PostRepositoryOnServer.DbFlagsList.ERROR_NETWORK -> throw IOException("ERROR postsViewModel.dbFlag.observe - ERROR_NETWORK")
                PostRepositoryOnServer.DbFlagsList.NONE -> {}
            }
            println("postsViewModel.dbFlag.observe   " + it)

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