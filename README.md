# Smart Waste Classifier — Android (Kotlin) + Flask API

Aplikasi Android untuk **mengidentifikasi jenis sampah** dan **subkategori** (mis. Plastik, Kaca, Kayu, dll.).
Frontend dibangun di **Android Studio (Kotlin/XML)**, dan model ML disajikan via **Flask API**.

> Catatan: Proyek ini masih banyak kekurangan. Banyak sisi yang masih bisa dilakukan pengembangan

---

## Fitur Utama
- 📸 Ambil gambar dari **kamera** atau **galeri**, kirim ke API untuk klasifikasi.
- 🧠 Prediksi dua level: **Organik/Anorganik** + **Subkategori** (17 label).
- 💾 Simpan hasil ke **Room Database** (lokal) beserta **path gambar**.
- 📊 **Pie Chart & Bar Chart** di halaman Profil + **riwayat** (RecyclerView).
- 🧭 **Bottom Navigation**: *Identifikasi* ↔ *Profil*.

---

## Tech Stack
**Android (Frontend)**
- Bahasa: Kotlin
- UI: Activity + XML (ConstraintLayout, CardView, RecyclerView)
- HTTP: OkHttp multipart upload
- Data lokal: Room (Entity/Dao/Database)
- Chart: MPAndroidChart (PieChart, BarChart)

**Backend**
- Python Flask + TensorFlow/Keras
- Endpoint: `POST /predict` (form field `file` berisi gambar)

---

## Cara Menjalankan — Android (Frontend)
1. Buka folder **android/** di **Android Studio**.
2. Pastikan permission berikut ada di `AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.CAMERA" />
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
   <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
   ```
3. (Jika butuh) Tambahkan dependency di `build.gradle` (Module):
   ```gradle
   implementation 'com.squareup.okhttp3:okhttp:4.12.0'        // versi contoh
   implementation 'androidx.room:room-runtime:2.6.1'
   kapt 'androidx.room:room-compiler:2.6.1'
   implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
   ```
4. **Konfigurasi URL API** di kode Android (mis. `MainActivity` / `IdentifikasiActivity`) pada variabel base URL `http://<HOST>:5000/predict`.
5. Jalankan di **emulator** atau **perangkat**. Pastikan perangkat dapat mengakses host API (satu jaringan/LAN atau gunakan IP publik).

---

## Cara Menjalankan — Backend Flask (Opsional)
> Hanya diperlukan jika Anda ingin menjalankan API klasifikasi sendiri (lokal/server).
```bash
cd backend
python -m venv .venv && source .venv/bin/activate
pip install flask tensorflow pillow numpy
python app.py
```
- Server default: `http://0.0.0.0:5000`
- Uji cepat:
  ```bash
  curl -F "file=@contoh.jpg" http://<HOST>:5000/predict
  # → {"kategori":"Organik","subkategori":"Sisa makanan"} (contoh)
  ```

**Spesifikasi Response**
```json
{ "kategori": "Organik|Anorganik", "subkategori": "<nama label>" }
```

---

## Alur Data Singkat
1. Pengguna mengambil/memilih gambar → Bitmap.
2. Aplikasi mengompresi ke JPEG → kirim via **multipart/form-data** (field `file`) ke `/predict`.
3. API memproses gambar: resize 224×224, normalisasi, model `MobileNetV2` memprediksi:
   - Level 1: Organik vs Anorganik
   - Level 2: 17 kelas subkategori
4. Android menampilkan hasil, menyimpan ke **Room** (label, timestamp, path image), dan memperbarui **riwayat + chart**.

---

## Database Lokal (Room)
- **Entity**: `SampahEntity(id, labelLevel1, labelLevel2, timestamp, imageUri)`
- **Dao**: `insertSampah()`, `getAll()`, `deleteAll()`
- **Nama DB**: `sampah_database`

---

## Navigasi & Layar
- **Identifikasi**: ambil foto/galeri, kirim ke API, tampilkan hasil → lanjut ke **Hasil**.
- **Hasil Klasifikasi**: preview gambar + label + tanggal; simpan ke DB (beserta path file).
- **Profil**: PieChart (Organik/Anorganik), BarChart (ringkasan harian), dan Riwayat (5 terbaru).

---

## Tips Deployment
- Jalankan Flask di VPS/Cloud (Gunicorn + Nginx) dan aktifkan **HTTPS**.
- Ubah **base URL** API di Android ke domain/host produksi.
- Pertimbangkan **rate limiting** & validasi ukuran file pada endpoint `/predict`.

## Dokumentasi
- **Page Identifikasi**
<img width="475" height="1013" alt="Screenshot 2025-08-09 102619" src="https://github.com/user-attachments/assets/2aafdf21-87d5-4bb7-8db7-f3febfc8de10" />

- **Page Profil**
<img width="466" height="1002" alt="Screenshot 2025-08-09 103039" src="https://github.com/user-attachments/assets/b4357039-3bbf-41f0-a666-cb33a3e8b389" />

- **Hasil Klasifikasi**
<img width="327" height="694" alt="Screenshot 2025-08-04 132734" src="https://github.com/user-attachments/assets/cfb56632-925f-4d07-ada5-b90853dfabc8" />


