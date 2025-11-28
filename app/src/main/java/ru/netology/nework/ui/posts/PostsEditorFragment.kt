package ru.netology.nework.ui.posts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.net.toUri
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentPostsEditorBinding
import ru.netology.nework.ui.retrofit.Post
import java.io.File

class PostsEditorFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val bottomNavigationView =
            requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView.visibility = View.GONE


        val binding = FragmentPostsEditorBinding.inflate(inflater, container, false)
        val viewModel by activityViewModels<PostsViewModel>()

        val post: Post? = arguments?.let {
            val gson = Gson()
            val token = TypeToken.getParameterized(Post::class.java).type
            val string = it.getString("post_to_postEditor")
            if (!string.isNullOrBlank()) gson.fromJson(string, token) else null
        }

        val urlPost = post?.attachment?.url

        val photoIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                if (activityResult.resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(context, "ImagePicker.RESULT_ERROR", Toast.LENGTH_LONG).show()
                    return@registerForActivityResult
                }
                val result = activityResult.data?.data ?: return@registerForActivityResult
                viewModel.changePhotoVM(result, result.toFile())
            }

        urlPost?.let {
            viewModel.changePhotoVM(
                urlPost.toUri(),
                urlPost.toUri().path?.let { File(it) },
            )
        }

        binding.contentPost.setText(post?.content)
        binding.contentPost.requestFocus()

        viewModel.photoLive.observe(viewLifecycleOwner) { photo ->
            if (photo == null) {
                binding.mediaContainerPost.isVisible = false
                return@observe
            } else {
                binding.mediaContainerPost.isVisible = true
            }

            val url = viewModel.photoLive.value?.uri
            Glide.with(binding.imagePost)
                .load(url)
//                .circleCrop()
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .into(binding.imagePost)
        }

        binding.removeImageButton.setOnClickListener {
            viewModel.removePhotoVM()
        }

        binding.makePhoto.setOnClickListener {
            println("INFO makePhoto pressed")
            ImagePicker.with(this)
                .cameraOnly()
                .crop()                    //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(
                    2048,
                    2048
                )    //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    photoIntentLauncher.launch(intent)
                }
        }

        binding.choosePhoto.setOnClickListener {
            println("INFO choosePhoto pressed")
            ImagePicker.with(this)
                .galleryOnly()
                .crop()
                .compress(1024)
                .maxResultSize(
                    2048,
                    2048
                )
                .galleryMimeTypes(arrayOf("image/png", "image/jpeg", "image/jpg"))
                .createIntent { intent ->
                    photoIntentLauncher.launch(intent)
                }
        }

//        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbarEditor)

        activity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_new_post, menu)
                println("INFO toolbarEditor's menu is inflated")
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                println("INFO toolbarEditor item selected $menuItem")
                when (menuItem.itemId) {
                    R.id.saveInToolbarEditor -> {
                        val text = binding.contentPost.text.toString()
                        if (text.isNotBlank()) {
                            viewModel.saveVM(text)
                        } else {
                            viewModel.cancelEditVM()
                        }
                        findNavController().navigateUp()
                        return true
                    }

                    R.id.homeInToolBarEditor -> {
                        viewModel.cancelEditVM()
                        findNavController().navigateUp()
                        return true
                    }

                    android.R.id.home -> {
                        viewModel.cancelEditVM()
                        findNavController().navigateUp()
                        return true
                    }

                    else -> return false
                }
            }
        }, viewLifecycleOwner)

        return binding.root
    }
}