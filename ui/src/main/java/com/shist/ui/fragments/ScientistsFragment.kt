package com.shist.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.shist.domain.ScientistItem
import com.shist.ui.adapters.ScientistsAdapter
import com.shist.ui.databinding.FragmentScientistsBinding
import org.koin.core.component.KoinComponent

class ScientistsFragment : Fragment(), KoinComponent {

    companion object {
        private const val KEY_SCIENTIST_LIST = "scientist_list"

        fun newInstance(scientistList: List<ScientistItem?>?)
        = ScientistsFragment().apply {
            arguments = Bundle().apply {
                putParcelableArrayList(KEY_SCIENTIST_LIST, ArrayList(scientistList?.toMutableList() ?: emptyList()))
            }
        }
    }

    private var _binding: FragmentScientistsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScientistsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val scientists = arguments?.getParcelableArrayList<ScientistItem>(KEY_SCIENTIST_LIST)?.toList()
        val adapter = ScientistsAdapter()
        adapter.submitList(scientists)
        binding.recyclerView.adapter = adapter
    }
}