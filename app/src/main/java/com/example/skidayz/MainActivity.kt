package com.example.skidayz

import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.Calendar

class MainActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {
    lateinit var datePickerButton: Button
    lateinit var fetchButton: Button
    lateinit var imageDesc: TextView
    lateinit var resourceTitle: TextView
    lateinit var imgView: ImageView
    lateinit var imageCopyright: TextView
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var lastLocation: Location? = null
    var api_id1 = "6619ba7a70e64481a70534eeb963a5c1"
    var day = 0
    var month: Int = 0
    var year: Int = 0
    var selectedDay = 0
    var selectedMonth: Int = 0
    var selectedYear: Int = 0
    var weatherCode: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        resourceTitle = findViewById<TextView>(R.id.resourceTitle)
        imgView = findViewById<ImageView>(R.id.imageView)
        imageCopyright = findViewById<TextView>(R.id.uVIndex)

        //imgView.visibility = View.INVISIBLE
        imgView.setImageResource(R.drawable.icon_sun_rain_foreground)

        imageDesc = findViewById<TextView>(R.id.imageDescription)
        fetchButton = findViewById(R.id.fetchButton)
        datePickerButton = findViewById(R.id.btnPick)
        datePickerButton.setOnClickListener {
            val calendar: Calendar = Calendar.getInstance()
            day = calendar.get(Calendar.DAY_OF_MONTH)
            month = calendar.get(Calendar.MONTH)
            year = calendar.get(Calendar.YEAR)
            val datePickerDialog =
                DatePickerDialog(this@MainActivity, this@MainActivity, year, month, day)
            // Don't allow for choosing dates in future
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }

        lastLocation = Location("dummyprovider")
        lastLocation?.latitude = 46.43569
        lastLocation?.longitude = -117.10090
    }

    fun fetchInfo(view: View) {
        getActualLocation()
        if (fetchButton.text.startsWith("Fetch", true)) {
            // Add vertical scrolling to the text view
            imageDesc.movementMethod = ScrollingMovementMethod()

            // Set image view to default image
            imgView.visibility = View.VISIBLE
            imgView.setImageResource(R.drawable.icon_cloud_foreground)

            resourceTitle.text = "Fetching"

            var url =
                "https://api.weatherbit.io/v2.0/current?" + "lat=" + lastLocation?.latitude + "&lon=" + lastLocation?.longitude + "&key=" + api_id1

            datePickerButton.visibility = View.INVISIBLE
            val queue = Volley.newRequestQueue(this)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    val dataObject = (response.getJSONArray("data")[0] as JSONObject)
                    resourceTitle.text = dataObject.getString("city_name")
                    weatherCode = dataObject.getJSONObject("weather").getInt("code")
                    determineWeatherIcon(weatherCode)
                },
                { error ->
                    if (error?.networkResponse == null) {
                        imageDesc.text = "Unknown Error with no response"
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
                        resourceTitle.text = "Error"
                        imageDesc.text =
                            "Unknown Error with message: $body status code: $statusCode."
                    }
                })
            fetchButton.text = "Choose another date"
            queue.add(jsonObjectRequest)
        } else {
            fetchButton.text = "Fetch Today's Information"
            datePickerButton.visibility = View.VISIBLE
            resourceTitle.text = "Welcome to Astronomy Picture Of The Day"

            imageDesc.visibility = View.VISIBLE
            imageDesc.text = ""
            imageCopyright.text = ""
            selectedDay = 0
            selectedMonth = 0
            selectedYear = 0
        }

    }

    private fun determineWeatherIcon(weatherCode: Int?) {
        if (weatherCode == null) {
            imgView.setImageResource(R.drawable.icon_sun_rain_foreground)
            return
        } else {
            // based on codes returned from https://www.weatherbit.io/api/codes, determine the weather icon to set
            // imgView.setImageResource() to the appropriate icon
            if (weatherCode > 199 && weatherCode < 234) {
                // thunderstorm
            } else if (weatherCode > 299 && weatherCode < 503) {
                // drizzle
                // rain
            } else if (weatherCode > 599 && weatherCode < 624) {
                // snow
                imgView.setImageResource(R.drawable.icon_snow_foreground)
            } else if (weatherCode > 700 && weatherCode < 782) {
                // atmosphere
                imgView.setImageResource(R.drawable.icon_cloud_foreground)
            } else if (weatherCode == 800) {
                // clear
                imgView.setImageResource(R.drawable.icon_sun_foreground)
            } else if (weatherCode > 800 && weatherCode < 805) {
                // clouds
                imgView.setImageResource(R.drawable.icon_cloud_foreground)
            } else {
                // default
                imgView.setImageResource(R.drawable.icon_cloud_foreground)
            }
        }
    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        selectedDay = p3
        selectedYear = p1
        selectedMonth = p2 + 1

        fetchButton.text = "Fetch Information for $selectedMonth-$selectedDay-$selectedYear"
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