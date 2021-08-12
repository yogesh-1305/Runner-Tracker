package com.example.runnertracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.runnertracker.databinding.ActivityMainBinding
import com.example.runnertracker.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*

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

        navigateToTrackingFragment(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragment(intent)
    }

    private fun navigateToTrackingFragment(intent: Intent?){
        if (intent?.action == ACTION_SHOW_TRACKING_FRAGMENT){
            fragmentContainerView.findNavController().navigate(R.id.action_global_tracking_fragment)
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


