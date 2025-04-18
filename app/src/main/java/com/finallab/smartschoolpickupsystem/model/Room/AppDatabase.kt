package com.finallab.smartschoolpickupsystem.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.finallab.smartschoolpickupsystem.DataModels.*
import com.finallab.smartschoolpickupsystem.Database.GuardianStudentDao

@Database(entities = [Student::class, Guardian::class, GuardianStudentCrossRef::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studentDao(): StudentDAO
    abstract fun guardianDao(): GuardianDAO
    abstract fun guardianStudentDao(): GuardianStudentDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                val newInstance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database_v35"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                instance = newInstance
                newInstance
            }
        }
    }
}
