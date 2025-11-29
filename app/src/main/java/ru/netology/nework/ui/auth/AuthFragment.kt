package ru.netology.nework.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.databinding.FragmentAuthBinding

@AndroidEntryPoint
class AuthFragment : Fragment() {

    private val authViewModel by activityViewModels<AuthViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuthBinding.inflate(layoutInflater, container, false)

        binding.authName.setText(authViewModel.data.login)
        binding.authPassword.setText(authViewModel.data.pass)

        binding.authOk.setOnClickListener {
            val name = binding.authName.text.toString()
            val password = binding.authPassword.text.toString()
            if (name.isNotBlank() && password.isNotBlank()) {
                Toast.makeText(
                    context,
                    "AUTHORIZATION in PROGRESS, please WAIT...",
                    Toast.LENGTH_LONG
                ).show()
                authViewModel.loginVM(name, password)
//                findNavController().navigateUp()
            } else {
                Toast.makeText(
                    context,
                    "Login and/or Password is empty, please try again",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        authViewModel.loginFaultFlag.observe(viewLifecycleOwner) { flag ->
            when (flag) {
                true -> {
                    Toast.makeText(
                        context,
                        "Login and/or Password is incorrect or INTERNET is OFF, please check & try again",
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