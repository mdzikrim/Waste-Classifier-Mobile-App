package com.example.tes.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sampah")
data class SampahEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val labelLevel1: String,  // Organik atau Anorganik
    val labelLevel2: String,  // Subkategori seperti Buah, Kaca, dll.
    val timestamp: String,    // Waktu klasifikasi
    val imageUri: String? = null  // URI gambar yang diklasifikasikan
)
