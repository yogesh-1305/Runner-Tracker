package com.example.runnertracker.fragments

import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.example.runnertracker.R
import com.example.runnertracker.databinding.FragmentSettingsBinding
import com.example.runnertracker.databinding.RunItemBinding
import com.example.runnertracker.other.Constants
import com.example.runnertracker.other.Constants.KEY_FIRST_TIME_USER
import com.example.runnertracker.other.Constants.KEY_NAME
import com.example.runnertracker.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private lateinit var binding: FragmentSettingsBinding

    @Inject
    lateinit var sharedPrefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setting saved username and weight to edit texts
        binding.setSettingUsernameEt.setText(sharedPrefs.getString(KEY_NAME, "") ?: "")
        binding.setSettingWeightEt.setText(sharedPrefs.getFloat(KEY_WEIGHT, 80f).toString())

        binding.setupSettingFragContinueButton.setOnClickListener {
            when (getUserDataAndUpdateInSharedPrefs()) {
                0 -> binding.setSettingUsernameEt.error = "Name Required"
                1 -> binding.setSettingWeightEt.error =
                    "Weight required to calculate your calories"
                true -> {
                    Snackbar.make(
                        binding.root,
                        "Changes Updated Successfully",
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
            }
        }

        listenToTextChanges()
    }

    private fun getUserDataAndUpdateInSharedPrefs(): Any {
        val name = binding.setSettingUsernameEt.text.toString().trim()
        val weight = binding.setSettingWeightEt.text.toString().trim()
        if (name.isEmpty()) {
            return 0
        }
        if (weight.isEmpty()) {
            return 1
        }
        sharedPrefs.edit().apply {
            putString(KEY_NAME, name)
            putFloat(KEY_WEIGHT, weight.toFloat())
            putBoolean(KEY_FIRST_TIME_USER, false)
        }.apply()
        return true
    }

    private fun listenToTextChanges() {
        binding.setSettingUsernameEt.addTextChangedListener {
            if (it?.length == 0){
                binding.setSettingUsernameEt.error = "Name Required"
            }else{
                binding.setSettingUsernameEt.error = null
            }
        }

        binding.setSettingWeightEt.addTextChangedListener {
            if (it?.length == 0){
                binding.setSettingWeightEt.error = "Weight required to calculate your calories"
            }else{
                binding.setSettingWeightEt.error = null
            }
        }
    }
}