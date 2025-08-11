package com.example.tes.ui.profil

data class HistoryItem(
        val labelLevel2: String,      // Label hasil klasifikasi level 2
        val timestamp: String,        // Tanggal dan waktu klasifikasi
        val imageUri: String? = null  // URI gambar yang diklasifikasi
)
