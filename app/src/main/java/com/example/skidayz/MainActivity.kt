package com.example.skidayz

import android.app.DatePickerDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import android.webkit.URLUtil
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
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
    var day = 0
    var month: Int = 0
    var year: Int = 0
    var selectedDay = 0
    var selectedMonth: Int = 0
    var selectedYear: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                "https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY"

            if (selectedDay != 0 && selectedMonth != 0 && selectedYear != 0) {
                url = "$url&date=$selectedYear-$selectedMonth-$selectedDay"
            }
            datePickerButton.visibility = View.INVISIBLE
            val queue = Volley.newRequestQueue(this)
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET, url, null,
                { response ->
                    resourceTitle.text = response.getString("title")
                    desc = response.getString("explanation")
                    copyright = if (response.has("copyright")) {
                        response.getString("copyright")
                    } else {
                        "no copyright"
                    }
                    mediaType = if (response.has("media_type")) {
                        response.getString("media_type")
                    } else {
                        ""
                    }
                    var resourceUrl = response.getString("url")
                    if (resourceUrl.isNullOrEmpty()) {
                        imageDesc.text = "Resource URL is empty"
                    } else if (!URLUtil.isValidUrl(resourceUrl)) {
                        imageDesc.text = "Resource URL is invalid"
                    } else {
                        var resourceURI = Uri.parse(resourceUrl)
                        if (resourceURI.scheme == "http") {
                            resourceURI = Uri.parse(resourceUrl.replace("http", "https"))
                        }
                        Glide.with(this).load(resourceURI.toString()).into(imgView)
                        when (getMediaType(this, resourceURI, mediaType)) {
                            MediaType.MediaTypeImage -> {
                                imgView.visibility = View.VISIBLE
                                videoDesc.visibility = View.INVISIBLE
                                videoCopyright.visibility = View.INVISIBLE
                                videoView.visibility = View.INVISIBLE
                                imageCopyright.text = copyright
                                imageDesc.text = desc
                                Glide.with(this).load(resourceURI.toString()).into(imgView)
                            }

                            MediaType.MediaTypeVideo -> {
                                imgView.visibility = View.INVISIBLE
                                videoView.visibility = View.VISIBLE
                                imageDesc.visibility = View.INVISIBLE
                                imageCopyright.visibility = View.INVISIBLE
                                videoDesc.text = desc
                                videoCopyright.text = copyright
                                videoView.setVideoURI(resourceURI)
                                val mediaController = MediaController(this)
                                mediaController.setAnchorView(videoView)
                                mediaController.setMediaPlayer(videoView)
                                videoView.setMediaController(mediaController)
                                videoView.start()
                            }

                            else -> {
                                imageDesc.text = "Unknown media type"
                            }
                        }
                    }
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