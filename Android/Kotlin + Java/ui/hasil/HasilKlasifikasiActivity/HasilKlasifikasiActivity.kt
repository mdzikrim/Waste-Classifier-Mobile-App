package com.example.tes.ui.hasil

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.tes.R
import androidx.lifecycle.lifecycleScope
import com.example.tes.data.AppDatabase
import com.example.tes.data.SampahEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.os.Environment
import java.io.File
import java.io.FileOutputStream

class HasilKlasifikasiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hasil_klasifikasi)

        val ivHasilGambar = findViewById<ImageView>(R.id.ivHasilGambar)
        val tvHasilKlasifikasi = findViewById<TextView>(R.id.tvHasilKlasifikasi)
        val tvTanggal = findViewById<TextView>(R.id.tvTanggal)
        val btnKembali = findViewById<Button>(R.id.btnKembali)

        val imageBytes = intent.getByteArrayExtra("image")
        val hasilKlasifikasi = intent.getStringExtra("hasil")
        val tanggal = intent.getStringExtra("tanggal")

        // Tampilkan gambar
        if (imageBytes != null) {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ivHasilGambar.setImageBitmap(bitmap)
        }

        // Tampilkan hasil klasifikasi
        tvHasilKlasifikasi.text = hasilKlasifikasi ?: "-"

        // Tampilkan tanggal
        tvTanggal.text = tanggal ?: "-"

        // Simpan Hasil ke Database
        if (imageBytes != null && hasilKlasifikasi != null && tanggal != null) {
            val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            val imagePath = saveBitmapToInternalStorage(bitmap) // <-- simpan file gambar

            val parts = hasilKlasifikasi.split(" - ")
            val kategori = parts.getOrNull(0) ?: "-"
            val subkategori = parts.getOrNull(1) ?: "-"

            lifecycleScope.launch(Dispatchers.IO) {
                val dao = AppDatabase.getInstance(applicationContext).sampahDao()
                val sampah = SampahEntity(
                    labelLevel1 = kategori,
                    labelLevel2 = subkategori,
                    timestamp = tanggal,
                    imageUri = imagePath
                )
                dao.insertSampah(sampah)
            }
        }

        btnKembali.setOnClickListener { finish() }
    }

    fun saveBitmapToInternalStorage(bitmap: Bitmap): String {
        val filename = "sampah_${System.currentTimeMillis()}.jpg"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), filename)
        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
        fos.flush()
        fos.close()
        return file.absolutePath
    }
}
