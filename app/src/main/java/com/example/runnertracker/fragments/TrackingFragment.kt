package com.example.runnertracker.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.runnertracker.databinding.FragmentTrackingBinding
import com.example.runnertracker.other.Constants.ACTON_PAUSE_SERVICE
import com.example.runnertracker.other.Constants.ACTON_START_OR_RESUME_SERVICE
import com.example.runnertracker.other.Constants.MAP_ZOOM
import com.example.runnertracker.other.Constants.POLYLINE_WIDTH
import com.example.runnertracker.other.Constants.POLY_LINE_COLOR
import com.example.runnertracker.services.PolyLine
import com.example.runnertracker.services.PolyLines
import com.example.runnertracker.services.TrackingService
import com.example.runnertracker.view_models.MainViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private var map: GoogleMap? = null
    private lateinit var binding: FragmentTrackingBinding

    private var isTracking = false
    private var pathCoordinates = mutableListOf<PolyLine>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
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
    }

    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTON_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTON_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking) {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.VISIBLE
        } else {
            btnToggleRun.text = "Stop"
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

    private fun sendCommandToService(action: String){
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireActivity().startService(it)
        }
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