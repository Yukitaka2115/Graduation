package com.cello

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.cello.testapk.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_PICK_IMAGE = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.button)

        button.setOnClickListener {
            showOptionsDialog()
        }
    }

    private fun showOptionsDialog() {
        val options = arrayOf("相机", "图库")

        AlertDialog.Builder(this)
            .setTitle("选择")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> openGallery()
                }
            }
            .show()
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        }
    }

    private fun openGallery() {
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickPhotoIntent.type = "image/*"
        startActivityForResult(pickPhotoIntent, REQUEST_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    // 相机返回
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    val imagePath = saveBitmapToFile(imageBitmap) // 保存位图到文件中
                    navigateToNextActivity(imagePath) // 传递位图路径
                }
                REQUEST_PICK_IMAGE -> {
                    // 图库返回
                    val selectedImageUri = data?.data
                    selectedImageUri?.let {
                        try {
                            val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                            val imagePath = saveBitmapToFile(imageBitmap) // 保存位图到文件中
                            navigateToNextActivity(imagePath) // 传递位图路径
                        } catch (e: IOException) {
                            // 处理 IO 异常
                            e.printStackTrace()
                        } catch (e: OutOfMemoryError) {
                            // 处理内存溢出异常
                            e.printStackTrace()
                            // 提示用户选择较小的图像文件或者实现适当的位图加载策略
                        }
                    }
                }
            }
        }
    }

    private fun saveBitmapToFile(bitmap: Bitmap): String {
        // 将位图保存到文件中，并返回文件路径
        val file = File(cacheDir, "image.jpg")
        file.createNewFile()

        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        return file.absolutePath
    }

    private fun navigateToNextActivity(imagePath: String) {
        val intent = Intent(this, DetectActivity::class.java)
        intent.putExtra("imagePath", imagePath)
        startActivity(intent)
    }


}
