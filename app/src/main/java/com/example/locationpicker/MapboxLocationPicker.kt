package com.example.locationpicker

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.core.content.ContextCompat
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.attribution.attribution
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.logo.logo
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider


class MapboxLocationPicker() {

    @SuppressLint("MissingPermission")
    fun init(
        context: Context,
        navigationLocationProvider: NavigationLocationProvider,
        mapView: MapView,
        accessToken: String,
        locationObserver: LocationObserver,
    ) {

        initStyle(mapView)
        applyMapToMapview(
            context,
            navigationLocationProvider,
            mapView
        )
        initNavigation(
            context,
            accessToken,
            locationObserver
            )
    }

    fun initStyle(mapView: MapView){
        mapView.getMapboxMap().loadStyleUri("mapbox://styles/mito2003/cl1e96y3n002f14l8hdc952yd")
        //mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
    }


    @SuppressLint("MissingPermission")
    fun initNavigation(context: Context, accessToken: String, locationObserver: LocationObserver) {
        MainActivity().mapboxNavigation = MapboxNavigation(
            NavigationOptions.Builder(context)
                .accessToken(accessToken)
                .build()
        ).apply {

            startTripSession(withForegroundService = false)

            registerLocationObserver(locationObserver)
        }
    }

    fun killMapboxNavigation(locationObserver: LocationObserver, mapboxNavigation: MapboxNavigation){
        //--------------------apply when Activity onDestroy()
        mapboxNavigation.stopTripSession()
        mapboxNavigation.unregisterLocationObserver(locationObserver)
    }

    fun updateCamera(location: Location, mapView: MapView) {

        //--------------------------camera settings
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        mapView.camera.flyTo(
            CameraOptions.Builder()
                .center(Point.fromLngLat(location.longitude, location.latitude))
                .zoom(16.0)
                .build(),
            mapAnimationOptions
        )
    }

    fun applyMapToMapview(context: Context, navigationLocationProvider: NavigationLocationProvider, mapView: MapView) {

        //--------------and the point where the user is
        mapView.location.apply {
            setLocationProvider(navigationLocationProvider)

            locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    context,
                    R.drawable.imhere
                )
            )

            enabled = true
        }

        //----------edit the map
        mapView.logo.updateSettings {
            enabled = false
        }

        mapView.attribution.updateSettings {
            enabled = false
        }

        mapView.scalebar.enabled = false
    }
}