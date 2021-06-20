package com.sonu.runningtrackerapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.PolylineOptions
import com.sonu.runningtrackerapp.R
import com.sonu.runningtrackerapp.services.Polyline
import com.sonu.runningtrackerapp.services.TrackingService
import com.sonu.runningtrackerapp.util.Constant
import com.sonu.runningtrackerapp.util.Constant.ACTION_PAUSE_SERVICE
import com.sonu.runningtrackerapp.util.Constant.ACTION_START_RESUME_SERVICE
import com.sonu.runningtrackerapp.util.Constant.ACTION_STOP_SERVICE
import com.sonu.runningtrackerapp.util.Constant.MAP_ZOOM
import com.sonu.runningtrackerapp.util.Constant.POLYLINE_COLOR
import com.sonu.runningtrackerapp.util.Constant.POLYLINE_WIDTH
import com.sonu.runningtrackerapp.util.TrackingUtility
import kotlinx.android.synthetic.main.fragment_tracking.*
import timber.log.Timber


class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var currentTimeInMills = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync {
            map = it
            addAllPolyLine()
        }
        btnToggleRun.setOnClickListener {
            toggleRun()
        }
        subscribeObservers()

    }

    private fun subscribeObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, {
            pathPoints = it
            addLastPolyline()
            moveCameraToUser()
        })
        TrackingService.timeRunsInMilliseconds.observe(viewLifecycleOwner, {
            currentTimeInMills = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMills, true)
            tvTimer.text = formattedTime
        })
    }

    private fun toggleRun() {
        if (isTracking) {
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        Timber.d("update tracking istraking " + isTracking)
        this.isTracking = isTracking

        if (!isTracking) {
            btnToggleRun.text = "Start"
            btnFinishRun.visibility = View.GONE
        } else {
            btnToggleRun.text = "Pause"
            btnFinishRun.visibility = View.VISIBLE
        }

    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun addAllPolyLine() {
        for (polyline in pathPoints) {
            val polyLineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polyLineOptions)
        }

    }

    private fun addLastPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLong = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLong = pathPoints.last().last()
            val polyLineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLong)
                .add(lastLatLong)

            map?.addPolyline(polyLineOptions)
        }
    }

    private fun sendCommandToService(action: String) {
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState) //for caching the map purpose
    }
}