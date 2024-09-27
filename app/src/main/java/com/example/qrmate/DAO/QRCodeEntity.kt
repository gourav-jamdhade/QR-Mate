package com.example.qrmate.DAO

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_table")
data class QRCodeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "qr_code_name") val qrCodeName: String,
    @ColumnInfo(name = "qr_code_path") val qrCodePath: String,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "user_id") val userId: String
)
