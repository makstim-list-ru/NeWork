package ru.netology.nework.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import dagger.hilt.android.AndroidEntryPoint
import okio.IOException
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentAuthRegBinding
import timber.log.Timber

@AndroidEntryPoint
class AuthRegFragment : Fragment() {

    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val photoIntentLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                if (activityResult.resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(context, "ImagePicker.RESULT_ERROR", Toast.LENGTH_LONG).show()
                    return@registerForActivityResult
                }
                val result = activityResult.data?.data ?: return@registerForActivityResult
                authViewModel.changePhotoVM(result, result.toFile())
            }

        val binding = FragmentAuthRegBinding.inflate(layoutInflater, container, false)

        binding.authRegLogin.setText(authViewModel.dataRegistration.login)
        binding.authRegFullName.setText(authViewModel.dataRegistration.fullName)
        binding.authRegPassword.setText(authViewModel.dataRegistration.pass)
        binding.authRegPasswordConfirmation.setText(authViewModel.dataRegistration.passConf)
        binding.authRegPhotoDummyAvatar.visibility = View.VISIBLE
        binding.authRegPhotoAvatar.visibility = View.GONE

        binding.authRegPhotoDummyAvatar.setOnClickListener {
            Timber.i("INFO PhotoDummy pressed")
            ImagePicker.with(this)
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

        binding.authRegPhotoAvatar.setOnClickListener {
            authViewModel.removePhotoVM()
            Glide.with(binding.authRegPhotoAvatar).clear(binding.authRegPhotoAvatar)
        }

        authViewModel.photoLive.observe(viewLifecycleOwner) { photo ->
            if (photo == null) {
                binding.authRegPhotoDummyAvatar.visibility = View.VISIBLE
                binding.authRegPhotoAvatar.visibility = View.GONE
                return@observe
            } else {
                binding.authRegPhotoDummyAvatar.visibility = View.GONE
                binding.authRegPhotoAvatar.visibility = View.VISIBLE
            }

            val url = authViewModel.photoLive.value?.uri
            Glide.with(binding.authRegPhotoAvatar)
                .load(url)
                .circleCrop()
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .into(binding.authRegPhotoAvatar)
        }

        binding.authRegOk.setOnClickListener {
            val newLogin = binding.authRegLogin.text.toString()
            val newFullName = binding.authRegFullName.text.toString()
            val newPass = binding.authRegPassword.text.toString()
            val newPassConf = binding.authRegPasswordConfirmation.text.toString()

            if (!(newLogin.isNotBlank() && newFullName.isNotBlank() && newPass.isNotBlank() && newPassConf.isNotBlank())) {
                Toast.makeText(
                    context,
                    "Login and/or Name and/or Password and/or Password Confirmation is empty, please try again",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (newPass != newPassConf) {
                Toast.makeText(
                    context,
                    "Password Confirmation is incorrect, please try again",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            authViewModel.loginRegVM(
                newLogin,
                newPass,
                newFullName,
            )
        }

        authViewModel.loginRegFaultFlag.observe(viewLifecycleOwner) { flag ->
            when (flag) {
                403 -> {
                    Toast.makeText(
                        context,
                        "Sorry, User with same Login has been registered already",
                        Toast.LENGTH_SHORT
                    ).show()
                    authViewModel.loginRegFaultClear()
                }

                415 -> {
                    Toast.makeText(
                        context,
                        "Photo format is INVALID, please, choose other one",
                        Toast.LENGTH_SHORT
                    ).show()
                    authViewModel.loginRegFaultClear()
                }

                200 -> {
                    authViewModel.loginRegFaultClear()
                    findNavController().navigateUp()
                }

                -10_000 -> {
                    Timber.i("INFO loginRegFaultFlag timeout ERROR most probably")
                    Toast.makeText(
                        context,
                        "Something is wrong, most probably INTERNET is OFF, please check & try again",
                        Toast.LENGTH_LONG
                    ).show()
                    authViewModel.loginRegFaultClear()
                }

                null -> {}

                else -> {
                    throw IOException("authViewModel.loginRegFaultFlag.observe(viewLifecycleOwner) - Invalid parameter")
                }
            }
        }

        return binding.root
    }
}