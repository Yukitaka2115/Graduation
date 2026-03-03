package com.cello

import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import android.util.Log

class Converter {

    data class DetectionResult(
        val x1: Float,
        val y1: Float,
        val x2: Float,
        val y2: Float,
        val label: String,
        val probability: Float
    )

    fun convert(responseData: String?, imageView: ImageView) {
        Log.d("Converter", "Response Data: $responseData")

        val listType = object : TypeToken<List<List<Any>>>() {}.type
        val results: List<List<Any>> = Gson().fromJson(responseData, listType)

        val detectionResults = results.map {
            DetectionResult(
                x1 = (it[0] as Double).toFloat(),
                y1 = (it[1] as Double).toFloat(),
                x2 = (it[2] as Double).toFloat(),
                y2 = (it[3] as Double).toFloat(),
                label = it[4] as String,
                probability = (it[5] as Double).toFloat()
            )
        }

        drawDetections(imageView, detectionResults)
    }

    private fun drawDetections(imageView: ImageView, detectionResults: List<DetectionResult>) {
        val originalBitmap = (imageView.drawable as BitmapDrawable).bitmap
        val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.STROKE
            strokeWidth = 5f
        }
        val textPaint = Paint().apply {
            color = Color.RED
            textSize = 40f
        }

        detectionResults.forEach { result ->
            Log.d("Converter", "Drawing box: (${result.x1}, ${result.y1}, ${result.x2}, ${result.y2}) with label: ${result.label}")
            canvas.drawRect(result.x1, result.y1, result.x2, result.y2, paint)
            canvas.drawText(result.label, result.x1, result.y1, textPaint)
        }

        imageView.setImageBitmap(mutableBitmap)
    }
}
