package com.sonu.runningtrackerapp.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.sonu.runningtrackerapp.R
import com.sonu.runningtrackerapp.db.entities.Run
import com.sonu.runningtrackerapp.services.Polyline
import com.sonu.runningtrackerapp.services.TrackingService
import com.sonu.runningtrackerapp.ui.viewmodels.MainViewModel
import com.sonu.runningtrackerapp.util.CancelTrackingDialog
import com.sonu.runningtrackerapp.util.Constant.ACTION_PAUSE_SERVICE
import com.sonu.runningtrackerapp.util.Constant.ACTION_START_RESUME_SERVICE
import com.sonu.runningtrackerapp.util.Constant.ACTION_STOP_SERVICE
import com.sonu.runningtrackerapp.util.Constant.FRAGMENT_CANCEL_TAG
import com.sonu.runningtrackerapp.util.Constant.MAP_ZOOM
import com.sonu.runningtrackerapp.util.Constant.POLYLINE_COLOR
import com.sonu.runningtrackerapp.util.Constant.POLYLINE_WIDTH
import com.sonu.runningtrackerapp.util.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_tracking.*
import timber.log.Timber
import java.util.*
import kotlin.math.round


@AndroidEntryPoint
class TrackingFragment : Fragment(R.layout.fragment_tracking) {
    private var map: GoogleMap? = null
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    private var currentTimeInMills = 0L
    private var menu: Menu? = null
    private var weight = 80f
    private val viewModel: MainViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)

        btnFinishRun.setOnClickListener {
            zoomToSeeWholeTrack()
            endRunAndSaveToDb()
            stopRun()
        }
        mapView.getMapAsync {
            map = it
            addAllPolyLine()
        }
        btnToggleRun.setOnClickListener {
            toggleRun()
        }
        subscribeObservers()

        if (savedInstanceState != null) {
            val dialog =
                parentFragmentManager.findFragmentByTag(FRAGMENT_CANCEL_TAG) as CancelTrackingDialog?
            dialog?.let {
                dialog.setYesListener {
                    stopRun()
                }
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        Timber.d("onprepare " + currentTimeInMills)
        if (currentTimeInMills > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    private fun showTrackingCancelledDialog() {
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, FRAGMENT_CANCEL_TAG)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miCancelTracking -> {
                showTrackingCancelledDialog()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment2)

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
            Timber.d("timeRuninmills " + it)
            currentTimeInMills = it
            val formattedTime = TrackingUtility.getFormattedStopWatchTime(currentTimeInMills, true)
            tvTimer.text = formattedTime
        })

    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && currentTimeInMills > 0) {
            btnToggleRun.text = resources.getString(R.string.startJob)
            btnFinishRun.visibility = View.GONE
        } else if (isTracking) {
            btnToggleRun.text = resources.getString(R.string.pauseJob)
            btnFinishRun.visibility = View.VISIBLE
            menu?.getItem(0)?.isVisible = true
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

    private fun endRunAndSaveToDb() {

        map?.snapshot {
            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolyLineLength(polyline).toInt()
            }
            var agSpeed =
                round((distanceInMeters / 1000f) / (currentTimeInMills / 1000f / 60 / 60) * 10) / 10f
            if (agSpeed.isNaN()) {
                agSpeed = 0f
            }
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()
            val run = Run(
                it,
                dateTimeStamp,
                distanceInMeters,
                agSpeed,
                currentTimeInMills,
                caloriesBurned
            )
            viewModel.insertRun(run)
            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run Saved Successfully",
                Snackbar.LENGTH_LONG
            )
                .show()
        }
    }

    private fun zoomToSeeWholeTrack() {
        if (!pathPoints.isEmpty() && !pathPoints[0].isEmpty()) {
            val bounds: LatLngBounds.Builder? = LatLngBounds.Builder()
            for (polyLine in pathPoints) {
                for (pos in polyLine) {
                    bounds!!.include(pos)
                }
            }
            map?.moveCamera(
                bounds?.let {
                    CameraUpdateFactory.newLatLngBounds(
                        bounds.build(),
                        mapView.width,
                        mapView.height,
                        (mapView.height * 0.05f).toInt()
                    )
                }
            )
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
        mapView?.let {
            mapView.onSaveInstanceState(outState)
        }  //for caching the map purpose
    }
}