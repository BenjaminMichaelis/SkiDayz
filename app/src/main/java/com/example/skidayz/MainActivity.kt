package com.example.skidayz

import android.app.DatePickerDialog
import android.content.Context
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
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
    lateinit var videoView: VideoView
    lateinit var videoCopyright: TextView
    lateinit var videoDesc: TextView
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var lastLocation: android.location.Location? = null
    var api_id1 = "6619ba7a70e64481a70534eeb963a5c1"
    var day = 0
    var month: Int = 0
    var year: Int = 0
    var selectedDay = 0
    var selectedMonth: Int = 0
    var selectedYear: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        resourceTitle = findViewById<TextView>(R.id.resourceTitle)
        imgView = findViewById<ImageView>(R.id.imageView)
        imageCopyright = findViewById<TextView>(R.id.imageCopyright)
        videoView = findViewById<VideoView>(R.id.videoView)
        videoView.visibility = View.INVISIBLE
        videoCopyright = findViewById<TextView>(R.id.videoCopyright)
        videoDesc = findViewById<TextView>(R.id.videoDescription)

        imgView.visibility = View.INVISIBLE
        imgView.setImageResource(R.drawable.ic_launcher_background)

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
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis;
            datePickerDialog.show()
        }

        lastLocation = Location("dummyprovider")
        lastLocation?.latitude = 46.43569
        lastLocation?.longitude = -117.10090
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            fusedLocationClient.getLastLocation()
//                .addOnSuccessListener { foundLocation: Location? ->
//                    if (foundLocation != null) {
//                        lastLocation = foundLocation
//                    } else {
//                        lastLocation = Location("dummyprovider")
//                        lastLocation?.latitude = 46.43569
//                        lastLocation?.longitude = -117.10090
//                    }
//                }
//        } else {
//            requestPermissions(
//                arrayOf(
//                    Manifest.permission.ACCESS_FINE_LOCATION,
//                    Manifest.permission.ACCESS_COARSE_LOCATION
//                ),
//                1
//            )
//        }
    }

    fun fetchInfo(view: View) {
        if (fetchButton.text.startsWith("Fetch", true)) {
            // Add vertical scrolling to the text view
            imageDesc.movementMethod = ScrollingMovementMethod()

            // Set image view to default image
            imgView.visibility = View.VISIBLE
            imgView.setImageResource(R.drawable.ic_launcher_background)

            resourceTitle.text = "Fetching"

            var desc = ""
            var copyright = ""
            var mediaType = ""

            var url =
                "https://api.weatherbit.io/v2.0/current?" + "lat=" + lastLocation?.latitude + "&lon=" + lastLocation?.longitude + "&key=" + api_id1

            datePickerButton.visibility = View.INVISIBLE
            val queue = Volley.newRequestQueue(this)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    val dataObject = (response.getJSONArray("data")[0] as JSONObject)
                    resourceTitle.text = dataObject.getString("city_name")
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

            imgView.visibility = View.INVISIBLE
            videoView.visibility = View.INVISIBLE
            imageDesc.visibility = View.VISIBLE
            videoDesc.visibility = View.INVISIBLE
            imageDesc.text = ""
            videoDesc.text = ""
            videoCopyright.text = ""
            imageCopyright.text = ""
            selectedDay = 0
            selectedMonth = 0
            selectedYear = 0
        }

    }

    override fun onDateSet(p0: DatePicker?, p1: Int, p2: Int, p3: Int) {
        selectedDay = p3
        selectedYear = p1
        selectedMonth = p2 + 1

        fetchButton.text = "Fetch Information for $selectedMonth-$selectedDay-$selectedYear"
    }
}

enum class MediaType {
    MediaTypeImage,
    MediaTypeVideo,
    Unknown
}

fun getMediaType(context: Context, source: Uri, mediaType: String): MediaType {
    if (mediaType.isNullOrBlank()) {
        val mediaTypeRaw = context.contentResolver.getType(source)
        if (mediaTypeRaw == null) {
            val path = source.path
            if (path != null) {
                return if (path.endsWith(".jpg", true) || path.endsWith(
                        ".jpeg",
                        true
                    ) || path.endsWith(
                        ".png",
                        true
                    )
                )
                    MediaType.MediaTypeImage
                else if (path.endsWith(".mp4", true) || path.endsWith(".mov", true))
                    MediaType.MediaTypeVideo
                else {
                    MediaType.Unknown
                }
            }
        } else if (mediaTypeRaw.startsWith("image", true))
            return MediaType.MediaTypeImage
        else if (mediaTypeRaw.startsWith("video", true))
            return MediaType.MediaTypeVideo
        return MediaType.Unknown
    } else if (mediaType.startsWith("image", true))
        return MediaType.MediaTypeImage
    else if (mediaType.startsWith("video", true))
        return MediaType.MediaTypeVideo
    return MediaType.Unknown
}