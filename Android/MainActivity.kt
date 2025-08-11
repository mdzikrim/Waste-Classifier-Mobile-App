package com.example.tes

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.ByteArrayOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var btnCamera: Button
    private lateinit var btnKlasifikasi: Button
    private lateinit var txtResult: TextView
    private var imageBitmap: Bitmap? = null
    private val CAMERA_REQUEST = 100
    private val CAMERA_PERMISSION_CODE = 101

    // URL Flask API
    private val url = "http://192.168.1.37:5000/predict"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        btnCamera = findViewById(R.id.btnCamera)
        btnKlasifikasi = findViewById(R.id.btnKlasifikasi)
        txtResult = findViewById(R.id.txtResult)

        btnCamera.setOnClickListener {
            checkCameraPermission()
        }

        btnKlasifikasi.setOnClickListener {
            if (imageBitmap == null) {
                Toast.makeText(this, "Ambil foto dulu!", Toast.LENGTH_SHORT).show()
            } else {
                uploadImageToFlask(imageBitmap!!)
            }
        }
    }

    // Izin Kamera
    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            openCamera()
        }
    }

    // Membuka kamera
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST)
    }

    // Menangani hasil izin kamera
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(this, "Izin kamera diperlukan untuk mengambil foto!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Menangani hasil foto dari kamera atau galeri
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val photo = data.extras?.get("data") as? Bitmap
            if (photo != null) {
                imageBitmap = photo
                imageView.setImageBitmap(photo)
                txtResult.text = ""
            } else {
                Toast.makeText(this, "Gagal mengambil gambar!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Mengirim gambar ke Flask server untuk klasifikasi
    private fun uploadImageToFlask(bitmap: Bitmap) {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream)
        val byteArray = stream.toByteArray()

        val client = OkHttpClient()

        // Membuat request body
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "capture.jpg",
                RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray))
            .build()

        // Membuat request ke Flask API
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    txtResult.text = "Error: ${e.localizedMessage}\n${e.message}\n${e.toString()}"
                }
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val res = response.body?.string()
                runOnUiThread {
                    txtResult.text = "Hasil klasifikasi: $res"
                }
            }
        })
    }
}
