package ru.netology.nework.ui.posts

import android.os.Bundle
import androidx.fragment.app.Fragment
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
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import ru.netology.nework.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import ru.netology.nework.databinding.FragmentPostsEditorBinding
import ru.netology.nework.ui.retrofit.Post
import java.io.File
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PostsEditorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PostsEditorFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val bottomNavigationView =
            requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigationView.visibility = View.GONE


        val binding = FragmentPostsEditorBinding.inflate(inflater, container, false)
        val viewModel by activityViewModels<PostsViewModel>()

        val postID = arguments?.getString("TEXT_TRANSFER")?.toLong()
        val post: Post? =
            null //TODO postID?.let { viewModel.data.asLiveData(Dispatchers.Default).value?.filter { it.id == postID }?.get(0) }
        val urlPost = post?.let { "http://10.0.2.2:9999/media/${post.attachment?.url}" }

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

        binding.content2.setText(post?.content)
        binding.content2.requestFocus()

        //binding.content2.setText(arguments?.getString("TEXT_TRANSFER"))

        viewModel.photoLive.observe(viewLifecycleOwner) { photo ->
            if (photo == null) {
                binding.photoContainer.isVisible = false
                return@observe
            } else {
                binding.photoContainer.isVisible = true
            }

            val url = viewModel.photoLive.value?.uri
            Glide.with(binding.photo)
                .load(url)
                .circleCrop()
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .into(binding.photo)
        }

        binding.removePhoto.setOnClickListener {
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
                        val text = binding.content2.text.toString()
                        if (text.isNotBlank()) {
                            viewModel.saveVM(text)
                        } else {
                            viewModel.cancelEditVM()
                            viewModel.removePhotoVM()
                        }
                        findNavController().navigateUp()
                        return true
                    }

                    R.id.homeInToolBarEditor -> {
                        viewModel.cancelEditVM()
                        viewModel.removePhotoVM()
                        findNavController().navigateUp()
                        return true
                    }

                    else -> return false
                }
            }
        }, viewLifecycleOwner)

        return binding.root


//        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_posts_editor, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment PostsEditorFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            PostsEditorFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}