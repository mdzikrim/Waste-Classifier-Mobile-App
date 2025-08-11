package com.example.tes.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SampahDao {
    @Insert
    suspend fun insertSampah(sampah: SampahEntity)

    @Query("SELECT * FROM sampah")
    suspend fun getAll(): List<SampahEntity>

    @Query("DELETE FROM sampah")
    suspend fun deleteAll()
}
