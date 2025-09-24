package ru.netology.nework.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentAuthRegBinding

@AndroidEntryPoint
class AuthRegFragment : Fragment() {

    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuthRegBinding.inflate(layoutInflater, container, false)

        binding.authRegLogin.setText(authViewModel.dataRegistration.login)
        binding.authRegFullName.setText(authViewModel.dataRegistration.fullName)
        binding.authRegPassword.setText(authViewModel.dataRegistration.pass)
        binding.authRegPasswordConfirmation.setText(authViewModel.dataRegistration.passConfirmation)
        binding.authRegPhotoAvatar.setImageResource(R.drawable.baseline_add_a_photo_24)

//        Glide.with(binding.authRegPhotoAvatar)
//            .load(R.drawable.baseline_add_a_photo_24)
////            .circleCrop()
//            .transform(CenterCrop(), RoundedCorners(16))
//            .placeholder(R.drawable.ic_loading_100dp)
//            .error(R.drawable.ic_error_100dp)
//            .timeout(10_000)
//            .into(binding.authRegPhotoAvatar)


        binding.authRegOk.setOnClickListener {
            val newLogin = binding.authRegLogin.text.toString()
            val newFullName = binding.authRegFullName.text.toString()
            val newPassword = binding.authRegPassword.text.toString()
            val newPasswordConfirmation = binding.authRegPasswordConfirmation.text.toString()

            if (!(newLogin.isNotBlank() && newFullName.isNotBlank() && newPassword.isNotBlank() && newPasswordConfirmation.isNotBlank())) {
                Toast.makeText(
                    context,
                    "Login and/or Name and/or Password and/or Password Confirmation is empty, please try again",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (newPassword != newPasswordConfirmation) {
                Toast.makeText(
                    context,
                    "Password Confirmation is incorrect, please try again",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

//            authViewModel.loginRegVM(
//                newLogin,
//                newFullName,
//                newPassword,
//                newPasswordConfirmation
//            )
        }

        authViewModel.loginRegFaultFlag.observe(viewLifecycleOwner) { flag ->
            when (flag) {
                true -> {
                    Toast.makeText(
                        context,
                        "Login and/or Password is incorrect, please try again",
                        Toast.LENGTH_LONG
                    ).show()
                    authViewModel.loginFaultClear()
                }

                false -> {
                    authViewModel.loginFaultClear()
                    findNavController().navigateUp()
                }

                else -> {}
            }
        }

        return binding.root
    }
}