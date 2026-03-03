package com.cello

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cello.testapk.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONException
import java.io.ByteArrayOutputStream
import java.io.IOException

@Suppress("DEPRECATION")
class DetectActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DetectActivity"
    }

    @SuppressLint("WrongThread")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detect)

        val imageView: ImageView = findViewById(R.id.imagePreview)
        val textView: TextView = findViewById(R.id.textView)
        val btn: Button = findViewById(R.id.recognizeButton)

        val originalBitmap: Bitmap? = intent.getParcelableExtra("bitmap")
        if (originalBitmap != null) {
            imageView.setImageBitmap(originalBitmap)
        } else {
            val imagePath = intent.getStringExtra("imagePath")
            if (imagePath != null) {
                val bitmapFromPath = BitmapFactory.decodeFile(imagePath)
                imageView.setImageBitmap(bitmapFromPath)

                val outputStream = ByteArrayOutputStream()
                bitmapFromPath.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                val bytes = outputStream.toByteArray()

                val hs = HttpService()
                btn.setOnClickListener {
                    val callback = object : Callback {
                        @SuppressLint("SetTextI18n")
                        override fun onFailure(call: Call, e: IOException) {
                            e.printStackTrace()
                            runOnUiThread {
                                textView.text = "识别失败"
                            }
                        }

                        @SuppressLint("SetTextI18n")
                        override fun onResponse(call: Call, response: Response) {
                            if (!response.isSuccessful) {
                                Log.e(TAG, "Server returned error: ${response.message}")
                                runOnUiThread {
                                    textView.text = "识别失败"
                                }
                                return
                            }
                            val responseData = response.body?.string()
                            if (responseData != null) {
                                Log.d(TAG, "Response: $responseData")

                                // Parse and format the response data
                                val formattedResponse = StringBuilder()
                                try {
                                    val jsonArray = JSONArray(responseData)
                                    for (i in 0 until jsonArray.length()) {
                                        val box = jsonArray.getJSONObject(i)
                                        val x1 = box.getDouble("x1")
                                        val y1 = box.getDouble("y1")
                                        val x2 = box.getDouble("x2")
                                        val y2 = box.getDouble("y2")
                                        val className = box.getString("class")
                                        val prob = box.getDouble("prob")

                                        formattedResponse.append("x1: $x1, y1: $y1, x2: $x2, y2: $y2, class: $className, prob: $prob\n")
                                    }
                                } catch (e: JSONException) {
                                    e.printStackTrace()
                                    runOnUiThread {
                                        textView.text = "解析错误"
                                    }
                                    return
                                }

                                runOnUiThread {
                                    textView.text = formattedResponse.toString()
                                }
                            }
                        }
                    }
                    hs.uploadImage(bytes, callback)
                }

            }
        }
    }
}







