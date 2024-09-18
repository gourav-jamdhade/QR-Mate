package com.example.qrmate.DAO

import android.content.Context
import androidx.room.Database
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper


@Database(entities = [QRCodeEntity::class], version = 1, exportSchema = false)
abstract class QRDatabase : RoomDatabase() {
   abstract fun qrCodeDao():QRCodeDao

   companion object{
       @Volatile
       private var INSTANCE: QRDatabase? = null

       fun getDatabase(context: Context):QRDatabase{
           return INSTANCE ?: synchronized(this){
               val instance = Room.databaseBuilder(
                   context.applicationContext,
                   QRDatabase::class.java,
                   "qr_database").build()
               INSTANCE = instance
               instance
       }
   }}}

