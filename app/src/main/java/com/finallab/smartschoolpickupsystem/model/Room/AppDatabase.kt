package com.finallab.smartschoolpickupsystem.Room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.finallab.smartschoolpickupsystem.DataModels.*
import com.finallab.smartschoolpickupsystem.Database.GuardianStudentDao
import com.finallab.smartschoolpickupsystem.GuardianDao
import com.finallab.smartschoolpickupsystem.GuardianEntity
import com.finallab.smartschoolpickupsystem.PickUpReport
import com.finallab.smartschoolpickupsystem.PickUpReportDao
import com.finallab.smartschoolpickupsystem.StudentDao
import com.finallab.smartschoolpickupsystem.StudentEntity

@Database(
    entities = [
        Student::class,
        Guardian::class,
        GuardianStudentCrossRef::class,
        StudentEntity::class,
        PickUpReport::class,
        GuardianEntity::class
    ],
    version = 3, // Increment version
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    // Admin-side DAOs
    abstract fun studentDao(): StudentDAO
    abstract fun guardianDao(): GuardianDAO
    abstract fun guardianStudentDao(): GuardianStudentDao

    // Parent-side DAOs
    abstract fun studentEntityDao(): StudentDao
    abstract fun pickUpReportDao(): PickUpReportDao
    abstract fun guardianEntityDao(): GuardianDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @JvmStatic
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "unified_app_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
