package com.example.runnertracker.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.runnertracker.R
import com.example.runnertracker.databinding.FragmentTrackingBinding
import com.example.runnertracker.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runnertracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runnertracker.other.Constants.ACTION_STOP_SERVICE
import com.example.runnertracker.other.Constants.MAP_ZOOM
import com.example.runnertracker.other.Constants.POLYLINE_WIDTH
import com.example.runnertracker.other.Constants.POLY_LINE_COLOR
import com.example.runnertracker.permissions.Permissions
import com.example.runnertracker.services.PolyLine
import com.example.runnertracker.services.TrackingService
import com.example.runnertracker.view_models.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private var map: GoogleMap? = null
    private lateinit var binding: FragmentTrackingBinding

    private var isTracking = false
    private var pathCoordinates = mutableListOf<PolyLine>()

    private var currentTimeInMillis = 0L

    private var menu: Menu? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView?.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        binding.mapView.getMapAsync {
            map = it
            addAllPolyLines()
        }
        subscribeToObservers()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, {
            updateTracking(it)
        })

        TrackingService.pathCoordinates.observe(viewLifecycleOwner, {
            pathCoordinates = it
            addLatestPolyLine()
            syncMapCenterWithUserLocation()
        })
        TrackingService.timeInMillis.observe(viewLifecycleOwner, {
            currentTimeInMillis = it
            val formattedTime = Permissions.getFormattedStopWatchTime(currentTimeInMillis, true)
            timerTextView.text = formattedTime
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else {
            btnToggleRun.text = "Stop"
            menu?.getItem(0)?.isVisible = true
            btnFinishRun.visibility = View.GONE
        }

    }

    private fun syncMapCenterWithUserLocation(){
        if (pathCoordinates.isNotEmpty() && pathCoordinates.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathCoordinates.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun addAllPolyLines(){
        for (polyline in pathCoordinates){
            val polylineOptions = PolylineOptions()
                .color(POLY_LINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyLine() {
        if (pathCoordinates.isNotEmpty() && pathCoordinates.last().size > 1){
            val secondLastCoordinate = pathCoordinates.last()[pathCoordinates.last().size - 2]
            val lastCoordinate = pathCoordinates.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLY_LINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(secondLastCoordinate)
                .add(lastCoordinate)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String){
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireActivity().startService(it)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.tracking_fragment_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeInMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cancel_run_menu_button -> showCancelRunDialog()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelRunDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Cancel the Rin?")
            .setMessage("Are you sure to cancel the run? This will delete all its data.")
            .setIcon(R.drawable.ic_baseline_delete_outline_24)
            .setPositiveButton("I'm Sure, Delete it"){ _, _ ->
                stopRun()
            }.setNegativeButton("No! Take me back") { dialogInterface, _ ->
                dialogInterface.cancel()
            }.create()
        dialog.show()
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }


}