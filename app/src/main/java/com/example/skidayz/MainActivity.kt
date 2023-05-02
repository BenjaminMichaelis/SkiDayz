package com.example.skidayz

import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    lateinit var fetchButton: Button
    lateinit var recommendations: TextView
    lateinit var locationName: TextView
    lateinit var imgView: ImageView
    lateinit var imageCopyright: TextView
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var coords: LatLng? = null
    var lastLocation: Location? = null
    var api_id1 = "6619ba7a70e64481a70534eeb963a5c1"
    var weatherCode: Int? = null
    var uv: Double? = null
    var clouds: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationName = findViewById<TextView>(R.id.resourceTitle)
        imgView = findViewById<ImageView>(R.id.imageView)
        imageCopyright = findViewById<TextView>(R.id.uVIndex)

        imgView.setImageResource(R.drawable.icon_sun_rain_foreground)

        recommendations = findViewById<TextView>(R.id.imageDescription)
        fetchButton = findViewById(R.id.fetchButton)

        getActualLocation()

        val mapFragment = supportFragmentManager.findFragmentById(
            R.id.map_fragment
        ) as? SupportMapFragment
        mapFragment?.getMapAsync { googleMap ->
            googleMap.setOnMapLoadedCallback {
                googleMap.uiSettings.isZoomControlsEnabled = true
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(50.0, -117.0
                )))

                if (lastLocation != null)
                {
                    googleMap.clear()
                    googleMap.addMarker(MarkerOptions().position(LatLng(lastLocation!!.latitude, lastLocation!!.longitude)))
                }
            }
            googleMap.setOnMapClickListener {
                googleMap.clear()
                googleMap.addMarker(MarkerOptions().position(it))

                lastLocation = Location(LocationManager.GPS_PROVIDER).apply {
                    latitude = it.latitude
                    longitude = it.longitude
                }
            }
        }
    }

    fun fetchInfo(view:View) {

        if(lastLocation == null)
        {
            getActualLocation()
        }

        if (fetchButton.text.startsWith("Fetch", true)) {
            // Add vertical scrolling to the text view
            recommendations.movementMethod = ScrollingMovementMethod()

            // Set image view to default image
            imgView.visibility = View.VISIBLE
            imgView.setImageResource(R.drawable.icon_cloud_foreground)

            locationName.text = "Fetching"

            if (lastLocation == null) {
                locationName.text = "Error"
                recommendations.text = "Unable to get location, please try again"
                getActualLocation()
                return
            }
            else
            {
                locationName.text = "Fetching"
                recommendations.text = ""
            }
            var url =
                "https://api.weatherbit.io/v2.0/current?" + "lat=" + lastLocation?.latitude + "&lon=" + lastLocation?.longitude + "&key=" + api_id1


            val queue = Volley.newRequestQueue(this)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    val dataObject = (response.getJSONArray("data")[0] as JSONObject)
                    locationName.text = dataObject.getString("city_name")
                    uv = dataObject.getDouble("uv")
                    clouds = dataObject.getInt("clouds")
                    weatherCode = dataObject.getJSONObject("weather").getInt("code")
                    determineWeatherIcon(weatherCode)
                    determineGoggleNeeds(clouds, uv)
                },
                { error ->
                    if (error?.networkResponse == null) {
                        recommendations.text = "Unknown Error with no response"
                    } else {
                        var body: String = ""
                        //get status code here
                        val statusCode: String =
                            java.lang.String.valueOf(error.networkResponse.statusCode)
                        //get response body and parse with appropriate encoding
                        try {
                            val utf8: Charset = Charset.forName("UTF-8")
                            body = JSONObject(
                                String(
                                    error.networkResponse.data,
                                    utf8
                                )
                            ).getString("msg")
                        } catch (e: UnsupportedEncodingException) {
                            // exception
                        }
                        locationName.text = "Error"
                        recommendations.text =
                            "Unknown Error with message: $body status code: $statusCode."
                    }
                })
            fetchButton.text = "Return to home page"
            queue.add(jsonObjectRequest)
        } else {
            fetchButton.text = "Fetch Location Information"
            locationName.text = "Welcome to SkiDayz"

            recommendations.visibility = View.VISIBLE
            recommendations.text = ""
            imageCopyright.text = ""
        }

    }

    private fun determineGoggleNeeds(clouds: Int?, uv: Double?) {
        when (clouds) {
            in 1..10 -> recommendations.text = ""
        }
    }

    private fun determineWeatherIcon(weatherCode: Int?) {
        if (weatherCode == null) {
            imgView.setImageResource(R.drawable.icon_sun_rain_foreground)
            return
        } else {
            // based on codes returned from https://www.weatherbit.io/api/codes, determine the weather icon to set
            // imgView.setImageResource() to the appropriate icon
            when (weatherCode) {
                in 200..233 -> {
                    // thunderstorm
                    imgView.setImageResource(R.drawable.icon_thunder_foreground)
                }
                in 300..502 -> {
                    // drizzle
                    // rain
                    imgView.setImageResource(R.drawable.icon_light_rain_foreground)
                }
                in 600..623 -> {
                    // snow
                    imgView.setImageResource(R.drawable.icon_snow_foreground)
                }
                in 701..781 -> {
                    // atmosphere
                    imgView.setImageResource(R.drawable.icon_cloud_foreground)
                }
                800 -> {
                    // clear
                    imgView.setImageResource(R.drawable.icon_sun_foreground)
                }
                in 801..804 -> {
                    // clouds
                    imgView.setImageResource(R.drawable.icon_cloud_foreground)
                }
                else -> {
                    // default
                    imgView.setImageResource(R.drawable.icon_cloud_foreground)
                }
            }
        }
    }

    private fun getActualLocation() {

        val task = fusedLocationClient.lastLocation

        if (ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED && ActivityCompat
                .checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
            return
        }

        task.addOnSuccessListener {
            if (it != null) {
                lastLocation = it
            }
        }
    } // one curly brace could be missing (or not)
}