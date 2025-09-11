package ru.netology.nework.ui.auth

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.databinding.FragmentAuthBinding
import kotlin.getValue

@AndroidEntryPoint
class AuthFragment : Fragment() {

    companion object {
        fun newInstance() = AuthFragment()
    }

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentAuthBinding.inflate(layoutInflater, container, false)

        val authViewModel by activityViewModels<AuthViewModel>()

        binding.authName.setText(authViewModel.data.login)
        binding.authPassword.setText(authViewModel.data.pass)

        binding.authOk.setOnClickListener {
            val name = binding.authName.text.toString()
            val passw = binding.authPassword.text.toString()
            if (name.isNotBlank() && passw.isNotBlank()) {
                Toast.makeText(
                    context,
                    "AUTHORIZATION in PROGRESS, please WAIT...",
                    Toast.LENGTH_LONG
                ).show()
                authViewModel.loginVM(name, passw)
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