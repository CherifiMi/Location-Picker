package com.example.locationpicker

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.locationpicker.databinding.ActivityMainBinding
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider


class MainActivity : AppCompatActivity() {

    //-----------------values
    lateinit var mapboxNavigation: MapboxNavigation
    private val navigationLocationProvider = NavigationLocationProvider()
    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {}
        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            val enhancedLocation = locationMatcherResult.enhancedLocation
            navigationLocationProvider.changePosition(
                enhancedLocation,
                locationMatcherResult.keyPoints,
            )
            if (moves){
                MapboxLocationPicker().updateCamera(enhancedLocation, binding.mapView)
                moves = false
            }
        }
    }

    private val FINE_LOCATION_RQ =101
    private lateinit var binding: ActivityMainBinding
    var moves = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //--------------Check Permissions
        checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION, FINE_LOCATION_RQ)

        //-------------------fly back to where the user is
        binding.backBtn.setOnClickListener{
            moves = true
        }

        //------------------get the coor where the pin is
        binding.coorBtn.setOnClickListener{
            Log.d("MYLOCATION", "Your location is " +
                    "${binding.mapView.getMapboxMap().cameraState.center.coordinates()}"
            )
        }

        MapboxLocationPicker().init(
            this,
            navigationLocationProvider,
            binding.mapView,
            getString(R.string.mapbox_access_token),
            locationObserver,
        )
    }



    private fun checkPermission(permission: String, requestCode: Int) {
        if (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this, "$name permission is granted", Toast.LENGTH_LONG).show()
        }else{
            requestPermissions(arrayOf(permission), requestCode)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MapboxLocationPicker().killMapboxNavigation(
            locationObserver,
            mapboxNavigation
        )
    }
}