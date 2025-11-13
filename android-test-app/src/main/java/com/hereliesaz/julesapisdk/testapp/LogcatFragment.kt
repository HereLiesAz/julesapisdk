package com.hereliesaz.julesapisdk.testapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.hereliesaz.julesapisdk.testapp.databinding.FragmentLogcatBinding
import kotlinx.coroutines.launch

class LogcatFragment : Fragment() {

    private var _binding: FragmentLogcatBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: LogcatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLogcatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = LogcatAdapter()
        // *** This is the correct ID from your fragment_logcat.xml ***
        binding.logcatList.layoutManager = LinearLayoutManager(requireContext())
        binding.logcatList.adapter = adapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.diagnosticLogs.collect { logs ->
                adapter.submitList(logs)
                if (logs.isNotEmpty()) {
                    // *** This is the correct ID from your fragment_logcat.xml ***
                    binding.logcatList.smoothScrollToPosition(logs.size - 1)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}