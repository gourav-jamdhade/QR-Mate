package com.example.qrmate.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QRCodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(qrCodeEntity: QRCodeEntity)

    @Query("SELECT * FROM qr_table ORDER BY timestamp DESC")
    fun getAllQRCodes(): List<QRCodeEntity>
}