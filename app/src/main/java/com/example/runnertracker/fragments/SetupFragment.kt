package com.example.runnertracker.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.runnertracker.R
import com.example.runnertracker.databinding.FragmentSetupBinding
import com.example.runnertracker.other.Constants.KEY_FIRST_TIME_USER
import com.example.runnertracker.other.Constants.KEY_NAME
import com.example.runnertracker.other.Constants.KEY_WEIGHT
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment : Fragment() {

    private lateinit var binding: FragmentSetupBinding

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    @set:Inject
    var isFirstTimeAppOpened = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetupBinding.inflate(inflater,container, false)
        if (!isFirstTimeAppOpened){
            findNavController().navigate(R.id.action_setupFragment_to_runFragment)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.setupFragContinueButton.setOnClickListener {
            when (getUserDataAndSaveInSharedPrefs()){
                0 -> binding.setUsernameLayout.error = "Name Required"
                1 -> binding.setWeightLayout.error = "Weight required to calculate your calories"
                true -> findNavController().navigate(R.id.action_setupFragment_to_runFragment)
            }
        }
    }

    private fun getUserDataAndSaveInSharedPrefs(): Any {
        val name = binding.setUsernameEt.text.toString().trim()
        val weight = binding.setWeightEt.text.toString().trim()
        if (name.isEmpty()){
            return 0
        }
        if (weight.isEmpty()){
            return 1
        }
        sharedPrefs.edit().apply {
            putString(KEY_NAME, name)
            putFloat(KEY_WEIGHT, weight.toFloat())
            putBoolean(KEY_FIRST_TIME_USER, false)
        }.apply()
        return true

    }
}