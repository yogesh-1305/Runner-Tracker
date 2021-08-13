package com.example.runnertracker.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.runnertracker.R
import com.example.runnertracker.adapters.RunAdapter
import com.example.runnertracker.databinding.FragmentRunBinding
import com.example.runnertracker.other.Constants.REQUEST_CODE_LOCATION_PERMISSIONS
import com.example.runnertracker.other.SortType
import com.example.runnertracker.other.TrackingUtility
import com.example.runnertracker.view_models.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

@AndroidEntryPoint
class RunFragment : Fragment(), EasyPermissions.PermissionCallbacks{

    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentRunBinding
    private lateinit var runAdapter: RunAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRunBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestLocationPermission()
        setupRecyclerview()

        viewModel.runs.observe(viewLifecycleOwner, {
            runAdapter.submitList(it)
        })

        binding.startRunButton.setOnClickListener{
            findNavController().navigate(R.id.action_runFragment_to_trackingFragment)
        }
    }

    private fun setupRecyclerview() = binding.rvRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun requestLocationPermission() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to provide location permissions to use the app",
                REQUEST_CODE_LOCATION_PERMISSIONS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to provide location Q permissions to use the app",
                REQUEST_CODE_LOCATION_PERMISSIONS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.sort_runs_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.sort_date -> {viewModel.sortRuns(sortType = SortType.DATE)}
            R.id.sort_time -> {viewModel.sortRuns(sortType = SortType.TIME)}
            R.id.sort_avg_speed -> {viewModel.sortRuns(sortType = SortType.AVG_SPEED)}
            R.id.sort_distance -> {viewModel.sortRuns(sortType = SortType.DISTANCE)}
            R.id.sort_calories -> {viewModel.sortRuns(sortType = SortType.CALORIES)}
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }else {
            requestLocationPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}