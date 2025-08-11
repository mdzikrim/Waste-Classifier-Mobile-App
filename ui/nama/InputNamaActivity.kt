package com.example.tes.ui.nama

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.tes.R
import com.example.tes.ui.identifikasi.IdentifikasiActivity

class InputNamaActivity : AppCompatActivity() {

    private lateinit var editTextNama: EditText
    private lateinit var tombolLanjut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs: SharedPreferences = getSharedPreferences("user_pref", MODE_PRIVATE)
        val namaTersimpan = prefs.getString("user_name", null)

        // Jika nama sudah ada, langsung ke halaman Identifikasi
        if (namaTersimpan != null) {
            startActivity(Intent(this, IdentifikasiActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_input_name)

        val editTextNama = findViewById<EditText>(R.id.etNama)
        val tombolLanjut = findViewById<Button>(R.id.btnSimpan)

        tombolLanjut.setOnClickListener {
            val nama = editTextNama.text.toString().trim()
            if (nama.isNotEmpty()) {
                prefs.edit().putString("user_name", nama).apply()
                startActivity(Intent(this, IdentifikasiActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Silakan masukkan nama terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
