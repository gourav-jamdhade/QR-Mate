package com.example.qrmate.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface QRCodeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(qrCodeEntity: QRCodeEntity)

    @Query("SELECT * FROM qr_table WHERE user_id= :userId ORDER BY timestamp DESC")
    fun getAllQRCodes(userId:String): List<QRCodeEntity>

    @Query("DELETE FROM qr_table WHERE id = :id")
    fun delete(id: String)


}