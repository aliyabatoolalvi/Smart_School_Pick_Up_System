package com.finallab.smartschoolpickupsystem.model.Repository

import android.util.Log
import com.finallab.smartschoolpickupsystem.DataModels.*
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GuardianStudentRepository(private val db: AppDatabase) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // ✅ Get all Guardians from Room
    fun getAllGuardians(): Flow<List<Guardian>> =
        db.guardianDao().getAllGuardians().flowOn(Dispatchers.IO)

    // ✅ Get all Students from Room
    fun getAllStudents(): Flow<List<Student>> =
        db.studentDao().getAllStudents().flowOn(Dispatchers.IO)

    // ✅ Insert Guardian
    suspend fun insertGuardian(guardian: Guardian) {
        db.guardianDao().insertGuardian(guardian)
    }

    // ✅ Insert Student
    suspend fun insertStudent(student: Student) {
        db.studentDao().insertStudent(student)
    }

    // ✅ Insert/Update Guardian
    suspend fun insertOrUpdateGuardian(guardian: Guardian) {
        db.guardianDao().upsertGuardian(guardian)
    }

    // ✅ Insert/Update Student
    suspend fun insertOrUpdateStudent(student: Student) {
        db.studentDao().upsertStudent(student)
    }

    // ✅ Delete Guardian
    suspend fun deleteGuardian(guardian: Guardian) {
        db.guardianDao().deleteGuardian(guardian)
    }

    // ✅ Delete Student
    suspend fun deleteStudent(student: Student) {
        db.studentDao().deleteStudent(student)
    }

    // ✅ Update Guardian
    suspend fun updateGuardian(guardian: Guardian) {
        db.guardianDao().updateGuardian(guardian)
    }

    // ✅ Update Student
    suspend fun updateStudent(student: Student) {
        db.studentDao().updateStudent(student)
    }

    // ✅ Get Guardians filtered by UserId
    suspend fun getGuardiansByUserId(userId: String): List<Guardian> {
        return db.guardianDao().getGuardiansByUserId(userId)
    }

    // ✅ Get Guardian by GuardianID (for EditGuardianActivity)
    suspend fun getGuardianById(guardianId: Int): Guardian? {
        return db.guardianDao().getGuardianById(guardianId)
    }

    // ✅ Get Student by StudentID (for EditStudentActivity)
    suspend fun getStudentById(studentId: Int): Student? {
        return db.studentDao().getStudentById(studentId)
    }

    // ✅ SYNC All Data from Firestore (No Duplicates!)
    suspend fun syncAllDataFromFirestore() = withContext(Dispatchers.IO) {
        try {
            val userId = auth.currentUser?.uid ?: return@withContext

            // 1. Sync Students
            val studentSnapshot = firestore.collection("students")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val students = studentSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Student::class.java)?.apply {
                    studentDocId = doc.id
                }
            }

            db.studentDao().deleteAllStudents()        // 🔥 Clear old students before inserting
            db.studentDao().insertAllStudents(students)

            Log.d("Sync", "Students Synced: ${students.size}")

            // 2. Sync Guardians
            val guardianSnapshot = firestore.collection("guardians")
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val guardians = guardianSnapshot.documents.mapNotNull { doc ->
                doc.toObject(Guardian::class.java)?.apply {
                    guardianDocId = doc.id
                }
            }

            db.guardianDao().deleteAllGuardians()      // 🔥 Clear old guardians too
            db.guardianDao().insertAllGuardians(guardians)

            Log.d("Sync", "Guardians Synced: ${guardians.size}")

            // 3. Sync CrossRefs (Guardian-Student)
            if (students.isNotEmpty()) {
                val studentDocIds = students.map { it.studentDocId }

                val crossRefSnapshot = firestore.collection("students")
                    .whereIn(FieldPath.documentId(), studentDocIds)
                    .get()
                    .await()

                db.guardianStudentDao().deleteAllCrossRefs()

                for (studentDoc in crossRefSnapshot.documents) {
                    val guardianIds = studentDoc.get("guardians") as? List<String> ?: continue
                    val studentId = students.firstOrNull { it.studentDocId == studentDoc.id }?.studentID ?: continue

                    for (guardianDocId in guardianIds) {
                        val guardian = guardians.firstOrNull { it.guardianDocId == guardianDocId } ?: continue
                        db.guardianStudentDao().insertGuardianStudentCrossRef(
                            GuardianStudentCrossRef(guardian.guardianID, studentId)
                        )
                    }
                }

                Log.d("Sync", "CrossRefs Synced")
            }

        } catch (e: Exception) {
            Log.e("SyncError", "Failed to sync data: ${e.message}")
        }
    }
}
