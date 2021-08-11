package com.example.runnertracker.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.runnertracker.R
import com.example.runnertracker.databinding.FragmentSetupBinding

class SetupFragment : Fragment() {

    private lateinit var binding: FragmentSetupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setHasOptionsMenu(true)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetupBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setupFragContinueButton.setOnClickListener {
            findNavController().navigate(R.id.action_setupFragment_to_runFragment)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.setup_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

}