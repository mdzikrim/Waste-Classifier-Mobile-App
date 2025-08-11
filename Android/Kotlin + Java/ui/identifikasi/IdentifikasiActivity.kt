package com.example.tes.ui.identifikasi

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tes.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.cardview.widget.CardView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.io.IOException
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class IdentifikasiActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val CAMERA_PERMISSION_CODE = 101

    private val url = "http://192.168.1.37:5000/predict"

    private var imageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_identifikasi)

        val btnIdentify = findViewById<CardView>(R.id.btnIdentify)
        val btnGaleri = findViewById<CardView>(R.id.btnGaleri)

        // Setup Bottom Navigation
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.selectedItemId = R.id.item_identifikasi
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_identifikasi -> true
                R.id.item_profil -> {
                    startActivity(Intent(this, com.example.tes.ui.profil.ProfilActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                else -> false
            }
        }

        // Tombol kamera (besar)
        btnIdentify.setOnClickListener {
            checkCameraPermission()
        }

        // Tombol galeri (kecil)
        btnGaleri.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_PICK)
        }
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            openCamera()
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
    }

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            val bitmap: Bitmap? = when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> data.extras?.get("data") as? Bitmap
                REQUEST_IMAGE_PICK -> MediaStore.Images.Media.getBitmap(this.contentResolver, data.data)
                else -> null
            }
            bitmap?.let {
                imageBitmap = it
                kirimGambarKeFlask(it)
            }
        }
    }

    // Kirim ke Flask API, lalu setelah dapat hasil, pindah ke HasilKlasifikasiActivity
    private fun kirimGambarKeFlask(gambar: Bitmap) {
        val client = OkHttpClient()

        val byteArrayOutputStream = ByteArrayOutputStream()
        gambar.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("file", "image.jpg", RequestBody.create("image/jpeg".toMediaTypeOrNull(), byteArray))
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@IdentifikasiActivity, "Gagal menghubungi server!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseText = response.body?.string()
                // Parsing hasil
                val kategori = Regex("\"kategori\"\\s*:\\s*\"([^\"]+)\"").find(responseText ?: "")?.groupValues?.get(1) ?: "-"
                val subkategori = Regex("\"subkategori\"\\s*:\\s*\"([^\"]+)\"").find(responseText ?: "")?.groupValues?.get(1) ?: "-"

                val hasil = "$kategori - $subkategori"
                val tanggal = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                // Pindah ke halaman hasil
                runOnUiThread {
                    pindahKeHasilKlasifikasi(imageBitmap!!, hasil, tanggal)
                }
            }
        })
    }

    // Fungsi pindah ke halaman hasil klasifikasi
    private fun pindahKeHasilKlasifikasi(imageBitmap: Bitmap, hasil: String, tanggal: String) {
        val intent = Intent(this, com.example.tes.ui.hasil.HasilKlasifikasiActivity::class.java)
        val stream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        val byteArray = stream.toByteArray()
        intent.putExtra("image", byteArray)
        intent.putExtra("hasil", hasil)
        intent.putExtra("tanggal", tanggal)
        startActivity(intent)
    }
}
