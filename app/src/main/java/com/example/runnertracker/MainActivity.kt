package com.example.runnertracker

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runnertracker.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var toolbar: Toolbar
    private lateinit var navController: NavController
//    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var destinationChangedListener: NavController.OnDestinationChangedListener
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        navController = findNavController(R.id.fragmentContainerView)
        bottomNavigationView = binding.bottomNavigationView
        bottomNavigationView.setupWithNavController(navController)

        destinationChangedListener = NavController.OnDestinationChangedListener{_, destination, _ ->
            when(destination.id){
                R.id.settingsFragment, R.id.runFragment, R.id.statsFragment ->
                    bottomNavigationView.visibility = View.VISIBLE
                else -> bottomNavigationView.visibility = View.GONE
            }
        }

        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.to_run_fragment -> {
//                    Navigation.findNavController(
//                        this, R.id.action_setupFragment_to_runFragment)
                }
            }
            true
        }
    }

    override fun onResume() {
        navController.addOnDestinationChangedListener(destinationChangedListener)
        super.onResume()
    }

    override fun onPause() {
        navController.removeOnDestinationChangedListener(destinationChangedListener)
        super.onPause()
    }
}


