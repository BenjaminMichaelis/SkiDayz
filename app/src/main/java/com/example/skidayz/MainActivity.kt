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
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset

class MainActivity : AppCompatActivity() {
    lateinit var fetchButton: Button
    lateinit var recommendations: TextView
    lateinit var locationName: TextView
    lateinit var imgView: ImageView
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var windInfo: TextView
    lateinit var snowInfo: TextView
    lateinit var uvIndexInfo: TextView
    lateinit var tempInfo: TextView
    lateinit var coordsInfo: TextView
    lateinit var visInfo: TextView
    var mapFragment: SupportMapFragment? = null
    var lastLocation: Location? = null
    var weatherBitApiKey = "6619ba7a70e64481a70534eeb963a5c1"
    var weatherCode: Int? = null
    var uv: Double? = null
    var clouds: Int? = null
    var recommendationsText: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationName = findViewById<TextView>(R.id.resourceTitle)
        imgView = findViewById<ImageView>(R.id.imageView)

        imgView.setImageResource(R.drawable.icon_sun_rain_foreground)

        recommendations = findViewById<TextView>(R.id.imageDescription)
        fetchButton = findViewById(R.id.fetchButton)

        windInfo = findViewById(R.id.wind)
        snowInfo = findViewById(R.id.snow)
        uvIndexInfo = findViewById(R.id.index)
        tempInfo = findViewById(R.id.temp)
        coordsInfo = findViewById(R.id.coordinates)
        visInfo = findViewById(R.id.vis)

        getActualLocation()

        mapFragment = supportFragmentManager.findFragmentById(
            R.id.map_fragment
        ) as? SupportMapFragment
        googleMapsSetup()
    }

    private fun googleMapsSetup() {
        mapFragment?.getMapAsync { googleMap ->
            googleMap.setOnMapLoadedCallback {
                googleMap.uiSettings.isZoomControlsEnabled = true
                googleMap.moveCamera(
                    CameraUpdateFactory.newLatLng(
                        LatLng(
                            50.0, -117.0
                        )
                    )
                )

                if (lastLocation != null) {
                    googleMap.clear()
                    googleMap.addMarker(
                        MarkerOptions().position(
                            LatLng(
                                lastLocation!!.latitude,
                                lastLocation!!.longitude
                            )
                        )
                    )
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

    fun fetchInfo(view: View) {
        getActualLocation()
        if (fetchButton.text.startsWith("Fetch Selected", true)) {
            // Add vertical scrolling to the text view
            recommendations.movementMethod = ScrollingMovementMethod()

            // Set image view to default image
            imgView.visibility = View.VISIBLE
            imgView.setImageResource(R.drawable.icon_cloud_foreground)

            locationName.text = "Fetching"

            if (lastLocation == null) {
                locationName.text = "Unknown Location"
                recommendations.text =
                    "Unable to get current location currently, please try again shortly or select a location on the map."
                getActualLocation()

            } else {
                locationName.text = "Fetching"
                recommendations.text = ""
                val searchLat = lastLocation?.latitude
                val searchLon = lastLocation?.longitude
                val url =
                    "https://api.weatherbit.io/v2.0/current?" + "lat=" + lastLocation?.latitude + "&lon=" + lastLocation?.longitude + "&key=" + weatherBitApiKey

                val queue = Volley.newRequestQueue(this)
                val jsonObjectRequest = JsonObjectRequest(
                    Request.Method.GET, url, null,
                    { response ->
                        val dataObject = (response.getJSONArray("data")[0] as JSONObject)
                        locationName.text = dataObject.getString("city_name")
                        uv = dataObject.getDouble("uv")
                        clouds = dataObject.getInt("clouds")
                        weatherCode = dataObject.getJSONObject("weather").getInt("code")
                        recommendations.text = ""
                        determineWeatherIcon(weatherCode)
                        determineGoggleNeeds(clouds)
                        determineSunscreenNeeds(uv)

                        recommendations.text = recommendationsText
                        recommendationsText = ""

                        snowInfo.text = "Snowfall: " + dataObject.getInt("precip")
                        windInfo.text = "Wind speed: " + dataObject.getInt("wind_spd")
                        uvIndexInfo.text = "UV Index: $uv"
                        tempInfo.text = "Temperature: " + dataObject.getInt("temp")
                        visInfo.text = "Visibility: " + dataObject.getInt("vis")
                        coordsInfo.text =
                            "Coordinates: \n \tLat: $searchLat\n \tLng:$searchLon"
                    },
                    { error ->
                        if (error?.networkResponse == null) {
                            recommendations.text = "Unknown Error with no response"
                        } else {
                            var body = ""
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
                queue.add(jsonObjectRequest)
            }
            fetchButton.text = "Fetch New Location Information"

        } else {
            fetchButton.text = "Fetch Selected/Current Location Information"
            locationName.text = "Welcome to SkiDayz"
            getActualLocation()
            googleMapsSetup()

            snowInfo.text = ""
            windInfo.text = ""
            uvIndexInfo.text = ""
            tempInfo.text = ""
            visInfo.text = ""
            coordsInfo.text =
                "Current location Coordinates: \n \tLat: " + lastLocation?.latitude + "\n \tLng:" + lastLocation?.longitude
            recommendations.visibility = View.VISIBLE
            recommendations.text = ""

        }
    }

    private fun determineSunscreenNeeds(uv: Double?) {
        if (uv == null) {
            recommendationsText += "Unexpected UV Index results. Please try again. "
            return
        }
        recommendationsText += "For sunscreen, "
        recommendationsText += when (uv) {
            in 0.0..2.9 -> "we would recommend SPF 15 or higher. "
            in 3.0..5.9 -> "we would recommend SPF 30 or higher. "
            in 6.0..7.9 -> "we would recommend SPF 50 or higher. "
            in 8.0..10.9 -> "we would recommend SPF 70 or higher. "
            in 11.0..100.0 -> "we would recommend SPF 100 or higher. "
            else -> "there are unexpected UV Index results. Please try again. "
        }
    }

    private fun determineGoggleNeeds(clouds: Int?) {
        if (clouds == null) {
            recommendationsText += "Unexpected UV Index results. Please try again. "
            return
        }
        recommendationsText += "For goggles, "
        recommendationsText += when (clouds) {
            in 0..25 -> "we would recommend wearing dark lenses (Platinum, black, red). "
            in 26..50 -> "we would recommend wearing semi-dark lenses (Blue, green, red). "
            in 51..100 -> "we would recommend wearing light lenses (Yellow, gold/copper, amber, rose). "
            else -> "there are unexpected Cloud coverage results. Please try again. "
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
            // Display prompt to request permissions if they are not already granted.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                101
            )
            return
        }

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            object : CancellationToken() {
                override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                    CancellationTokenSource().token

                override fun isCancellationRequested() = false
            })
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
                    task.addOnSuccessListener {
                        if (it != null) {
                            lastLocation = it
                        }
                    }
                } else {
                    lastLocation = location
                }
            }
    }
}