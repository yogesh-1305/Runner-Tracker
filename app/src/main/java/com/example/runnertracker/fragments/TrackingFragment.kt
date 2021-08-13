package com.example.runnertracker.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.runnertracker.R
import com.example.runnertracker.databinding.FragmentTrackingBinding
import com.example.runnertracker.db.Run
import com.example.runnertracker.other.Constants
import com.example.runnertracker.other.Constants.ACTION_PAUSE_SERVICE
import com.example.runnertracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.runnertracker.other.Constants.ACTION_STOP_SERVICE
import com.example.runnertracker.other.Constants.MAP_ZOOM
import com.example.runnertracker.other.Constants.POLYLINE_WIDTH
import com.example.runnertracker.other.Constants.POLY_LINE_COLOR
import com.example.runnertracker.other.TrackingUtility
import com.example.runnertracker.services.PolyLine
import com.example.runnertracker.services.TrackingService
import com.example.runnertracker.view_models.MainViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private var map: GoogleMap? = null
    private lateinit var binding: FragmentTrackingBinding

    private var isTracking = false
    private var pointToMyLocation = false
    private var pathCoordinates = mutableListOf<PolyLine>()

    private var currentTimeInMillis = 0L

    @set:Inject
    private var weight = 80f

    private var menu: Menu? = null

    @Inject
    @Named("for_fragment")
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        binding.showMyLocationBtn.setOnClickListener {
            pointToMyLocation = true
            pointToMyLocation()
        }

        binding.btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
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
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMillis, true)
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

    private fun syncMapCenterWithUserLocation() {
        if (pathCoordinates.isNotEmpty() && pathCoordinates.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathCoordinates.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun pointToMyLocation() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            val request = LocationRequest().apply {
                interval = Constants.LOCATION_UPDATE_INTERVAL
                fastestInterval = Constants.FASTEST_LOCATION_UPDATE_INTERVAL
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            p0.locations.let { locations ->
                if (pointToMyLocation) {
                    for (location in locations) {
                        val latLng = LatLng(location.latitude, location.longitude)
                        map?.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(
                                latLng,
                                MAP_ZOOM
                            )
                        )
                    }
                    pointToMyLocation = false
                }
            }
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.builder()
        for (polyline in pathCoordinates) {
            for (position in polyline) {
                bounds.include(position)
            }
        }
        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                mapView.width,
                mapView.height,
                (mapView.width * 0.05f).toInt()
            )
        )
    }

    private fun endRunAndSaveToDb() {
        map?.snapshot { bmp ->
            var distanceInMeters = 0
            for (polyline in pathCoordinates) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }
            val avgSpeedInKMPH =
                round((distanceInMeters / 1000f) / (currentTimeInMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(bmp, dateTimeStamp, avgSpeedInKMPH, distanceInMeters, currentTimeInMillis, caloriesBurned)
            viewModel.insertRun(run)
            Snackbar.make(binding.root,
            "Run Save Successfully",
            Snackbar.LENGTH_LONG).show()
            stopRun()
        }
    }

    private fun addAllPolyLines() {
        for (polyline in pathCoordinates) {
            val polylineOptions = PolylineOptions()
                .color(POLY_LINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyLine() {
        if (pathCoordinates.isNotEmpty() && pathCoordinates.last().size > 1) {
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

    private fun sendCommandToService(action: String) {
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
            .setTitle("Cancel the Run?")
            .setMessage("Are you sure to cancel the run? This will delete all the current run's data.")
            .setIcon(R.drawable.ic_baseline_delete_outline_24)
            .setPositiveButton("I'm Sure, Delete it") { _, _ ->
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