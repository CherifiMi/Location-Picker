package com.example.locationpicker

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.example.locationpicker.databinding.ActivityMainBinding
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.overlay.mapboxOverlay
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider


class MainActivity : AppCompatActivity() {

    //-----------------values
    private val navigationLocationProvider = NavigationLocationProvider()

    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {

        }
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )

            if (moves){
                updateCamera(enhancedLocation)
                moves = false
            }


        }
    }
    private lateinit var mapboxMap: MapboxMap
    private lateinit var mapboxNavigation: MapboxNavigation

    private val FINE_LOCATION_RQ =101

    private lateinit var binding: ActivityMainBinding

    var moves = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, "Location", FINE_LOCATION_RQ)

        mapboxMap = binding.mapView.getMapboxMap()
        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
        }
        binding.backBtn.setOnClickListener{
            moves = true
        }

        binding.coorBtn.setOnClickListener{
            Log.d("MYLOCATION", "Your location is " +
                    "${binding.mapView.getMapboxMap().cameraState.center.coordinates()}"
            )
        }

        init()
    }

    private fun checkPermission(permission: String, name: String, requestCode: Int) {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "$name permission is granted", Toast.LENGTH_LONG).show()
        }else{
            requestPermissions(arrayOf(permission), requestCode)
        }
    }


    private fun init() {
        initStyle()
        initNavigation()
    }

    private fun initStyle() {
        mapboxMap.loadStyleUri("mapbox://styles/mito2003/cl1e96y3n002f14l8hdc952yd")
    }

    @SuppressLint("MissingPermission")
    private fun initNavigation() {
        mapboxNavigation = MapboxNavigation(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        ).apply {

            startTripSession()

            registerLocationObserver(locationObserver)
        }
    }

    private fun updateCamera(location: Location) {

        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        binding.mapView.camera.flyTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(location.longitude, location.latitude))
                .zoom(16.0)
                .build(),
            mapAnimationOptions
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mapboxNavigation.stopTripSession()
        mapboxNavigation.unregisterLocationObserver(locationObserver)
    }

}