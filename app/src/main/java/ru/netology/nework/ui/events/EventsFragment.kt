package ru.netology.nework.ui.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.databinding.FragmentEventsBinding

@AndroidEntryPoint
class EventsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val eventsViewModel by viewModels<EventsViewModel>()

        val binding = FragmentEventsBinding.inflate(inflater, container, false)

        eventsViewModel.text.observe(viewLifecycleOwner) {
            binding.textDashboard.text = it
        }
        return binding.root
    }
}