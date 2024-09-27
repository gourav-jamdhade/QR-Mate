package com.example.qrmate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var fusedLoactionClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    //private lateinit var searchView: SearchView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_map)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        Places.initialize(applicationContext, "AIzaSyD05Z-T8S4iL2kGX7jpWGw4k_4XN7EcBsE")
        placesClient = Places.createClient(this)


        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        fusedLoactionClient = LocationServices.getFusedLocationProviderClient(this)

//        searchView = findViewById(R.id.searchView)
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                if (query != null) {
//                    searchLocation(query)
//                }
//
//                return false
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                return false
//            }
//
//        })

    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            map.isMyLocationEnabled = true
            fusedLoactionClient.lastLocation.addOnSuccessListener { location: Location? ->

                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
        }

        map.setOnMapClickListener { latLng ->
            map.clear()
            map.addMarker(MarkerOptions().position(latLng))
            val coordinates = "${latLng.latitude},${latLng.longitude}"

            val intent = Intent()
            intent.putExtra("coordinates", coordinates)
            setResult(RESULT_OK, intent)
            finish()

        }
    }


//    private fun searchLocation(query: String) {
//        val request = FindAutocompletePredictionsRequest.builder()
//            .setQuery(query)
//            .build()
//
//        placesClient.findAutocompletePredictions(request)
//            .addOnSuccessListener { response ->
//
//                for (predictions: AutocompletePrediction in response.autocompletePredictions) {
//                    Log.i("Places", predictions.placeId)
//                    fetchPlaceDetails(predictions.placeId)
//                }
//            }.addOnFailureListener { exception ->
//                Log.e("Places", "Place not found: ${exception.message}")
//                Toast.makeText(this, "Place not found: ${exception.message}", Toast.LENGTH_SHORT)
//                    .show()
//            }
//    }

//    private fun fetchPlaceDetails(placeId: String) {
//
//        val placeFields = listOf(Place.Field.LAT_LNG)
//        val request = FetchPlaceRequest.builder(placeId, placeFields).build()
//
//        placesClient.fetchPlace(request)
//            .addOnSuccessListener { response ->
//
//                val place = response.place
//                val latLng = place.latLng
//                if(latLng != null){
//                    map.clear()
//                    map.addMarker(MarkerOptions().position(latLng))
//                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
//                }
//            }
//            .addOnFailureListener { exception ->
//                Log.e("Places", "Place not found: ${exception.message}")
//                Toast.makeText(this, "Place not found: ${exception.message}", Toast.LENGTH_SHORT)
//                    .show()
//            }
//
//    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getCurrentLocation()
        }
    }
}