package com.finallab.smartschoolpickupsystem.model.Repository;

import android.util.Log;
import com.finallab.smartschoolpickupsystem.DataModels.*;
import com.finallab.smartschoolpickupsystem.Room.AppDatabase;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.flow.Flow;
import kotlinx.coroutines.flow.first;
import kotlinx.coroutines.tasks.await;
import kotlinx.coroutines.withContext;

class GuardianStudentRepository(private val db: AppDatabase) {

    private val firestore = FirebaseFirestore.getInstance();
    suspend fun getStudentById(studentId: Int): Student? {
        return db.studentDao().getStudentById(studentId)
    }

    suspend fun getGuardianById(guardianId: Int): Guardian? {
        return db.guardianDao().getGuardianById(guardianId)
    }

    // ✅ Insert Guardian into Room and Firestore
//    suspend fun insertGuardian(guardian: Guardian) = withContext(Dispatchers.IO) {
//        val guardianId = db.guardianDao().insertGuardian(guardian);
//
//        val guardianMap = guardian.toMap();
//
//        firestore.collection("Guardians")
//            .document(guardian.guardianID.toString())
//            .set(guardianMap, SetOptions.merge())
//            .addOnSuccessListener { Log.d("Firestore", "Guardian added successfully.") }
//            .addOnFailureListener { Log.e("Firestore", "Error adding guardian", it) };
//    }

    suspend fun insertGuardian(guardian: Guardian) = withContext(Dispatchers.IO) {
        try {
            // Insert Guardian into Room database
            val guardianId = db.guardianDao().insertGuardian(guardian)

            // Convert Guardian object to Map for Firestore
            val guardianMap = guardian.toMap()

            // Store Guardian in Firestore 'Guardians' collection
            firestore.collection("guardians")
                .document(guardian.guardianID.toString())
                .set(guardianMap, SetOptions.merge())
                .addOnSuccessListener { Log.d("Firestore", "Guardian added successfully.") }
                .addOnFailureListener { Log.e("Firestore", "Error adding guardian", it) }

            // Add Guardian to the related Student's 'guardians' array
            firestore.collection("students")
                .document(guardian.studentDocumentID)
                .update("guardians", FieldValue.arrayUnion(guardianMap))
                .addOnSuccessListener { Log.d("Firestore", "Guardian linked to student.") }
                .addOnFailureListener { Log.e("Firestore", "Error linking guardian to student", it) }

        } catch (e: Exception) {
            Log.e("Firestore", "Error inserting guardian", e)
        }
    }


    // ✅ Update Guardian in Room and Firestore
    suspend fun updateGuardian(guardian: Guardian) = withContext(Dispatchers.IO) {
        db.guardianDao().updateGuardian(guardian);

        firestore.collection("guardians")
            .document(guardian.guardianID.toString())
            .set(guardian.toMap(), SetOptions.merge())
            .addOnSuccessListener { Log.d("Firestore", "Guardian updated successfully.") }
            .addOnFailureListener { Log.e("Firestore", "Error updating guardian", it) };
    }
    // ✅ Upsert Guardian in Room and Firestore
    suspend fun insertOrUpdateGuardian(guardian: Guardian) = withContext(Dispatchers.IO) {
        db.guardianDao().upsertGuardian(guardian) // Insert or update Guardian in Room

        firestore.collection("guardians")
            .document(guardian.guardianID.toString())
            .set(guardian.toMap(), SetOptions.merge()) // Merge data in Firestore
            .addOnSuccessListener { Log.d("Firestore", "Guardian synced successfully.") }
            .addOnFailureListener { Log.e("Firestore", "Error syncing guardian", it) }
    }

  suspend fun deleteGuardian(guardian: Guardian) = withContext(Dispatchers.IO) {
        try {
            // Delete Guardian from Room database
            db.guardianDao().deleteGuardian(guardian)

            // Delete Guardian from Firestore 'Guardians' collection
            firestore.collection("guardians")
                .document(guardian.guardianID.toString())
                .delete()
                .addOnSuccessListener { Log.d("Firestore", "Guardian deleted successfully.") }
                .addOnFailureListener { Log.e("Firestore", "Error deleting guardian", it) }

            // Remove Guardian from Student's 'guardians' array
            firestore.collection("students")
                .document(guardian.studentDocumentID)
                .update("guardians", FieldValue.arrayRemove(guardian.toMap()))
                .addOnSuccessListener { Log.d("Firestore", "Guardian removed from student.") }
                .addOnFailureListener { Log.e("Firestore", "Error removing guardian from student", it) }

        } catch (e: Exception) {
            Log.e("Firestore", "Error deleting guardian", e)
        }
    }

    suspend fun getGuardiansByUserId(userId: String): List<Guardian> {
        return db.guardianDao().getGuardiansByUserId(userId)
    }
    // ✅ Insert Student into Room and Firestore
    suspend fun insertStudent(student: Student) = withContext(Dispatchers.IO) {
        val studentId = db.studentDao().insertStudent(student);

        firestore.collection("students")
            .document(student.studentID.toString())
            .set(student.toMap(), SetOptions.merge())
            .addOnSuccessListener { Log.d("Firestore", "Student added successfully.") }
            .addOnFailureListener { Log.e("Firestore", "Error adding student", it) };
    }

    // ✅ Update Student in Room and Firestore
    suspend fun updateStudent(student: Student) = withContext(Dispatchers.IO) {
        db.studentDao().updateStudent(student);

        firestore.collection("students")
            .document(student.studentID.toString())
            .set(student.toMap(), SetOptions.merge())
            .addOnSuccessListener { Log.d("Firestore", "Student updated successfully.") }
            .addOnFailureListener { Log.e("Firestore", "Error updating student", it) };
    }
    // ✅ Upsert Student in Room and Firestore
    suspend fun insertOrUpdateStudent(student: Student) = withContext(Dispatchers.IO) {
        db.studentDao().upsertStudent(student) // Insert or update Student in Room

        firestore.collection("students")
            .document(student.studentID.toString())
            .set(student.toMap(), SetOptions.merge()) // Merge data in Firestore
            .addOnSuccessListener { Log.d("Firestore", "Student synced successfully.") }
            .addOnFailureListener { Log.e("Firestore", "Error syncing student", it) }
    }

    // ✅ Delete Student from Room and Firestore
    suspend fun deleteStudent(student: Student) = withContext(Dispatchers.IO) {
        db.studentDao().deleteStudent(student);

        firestore.collection("students")
            .document(student.studentID.toString())
            .delete()
            .addOnSuccessListener { Log.d("Firestore", "Student deleted successfully.") }
            .addOnFailureListener { Log.e("Firestore", "Error deleting student", it) };
    }

    // ✅ Get all Guardians (from Room using Flow)
    fun getAllGuardians(): Flow<List<Guardian>> = db.guardianDao().getAllGuardians();

    // ✅ Get all Students (from Room using Flow)
    fun getAllStudents(): Flow<List<Student>> = db.studentDao().getAllStudents();

    // ✅ Get Students for a Guardian
    fun getStudentsForGuardian(guardianId: Int): Flow<GuardianWithStudents?> {
        return db.guardianStudentDao().getGuardianWithStudents(guardianId)
    }

    // ✅ Get Guardians for a Student
    fun getGuardiansForStudent(studentId: Int): Flow<StudentWithGuardians?> {
        return db.guardianStudentDao().getStudentWithGuardians(studentId)
    }


    // ✅ Sync Guardians from Firestore to Room
    suspend fun syncGuardiansFromFirestore() = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("guardians").get().await()
            val guardians = snapshot.toObjects(Guardian::class.java)
            guardians.forEach { db.guardianDao().upsertGuardian(it) }
            Log.d("Firestore", "Guardians synced successfully.")
        } catch (e: Exception) {
            Log.e("Firestore", "Error syncing guardians", e)
        }
    }

    // ✅ Sync Students from Firestore to Room
    suspend fun syncStudentsFromFirestore() = withContext(Dispatchers.IO) {
        try {
            val snapshot = firestore.collection("students").get().await()
            val students = snapshot.toObjects(Student::class.java)
            students.forEach { db.studentDao().upsertStudent(it) }
            Log.d("Firestore", "Students synced successfully.")
        } catch (e: Exception) {
            Log.e("Firestore", "Error syncing students", e)
        }
    }



    suspend fun syncGuardianWithFirestore(guardian: Guardian, callback: (Boolean, String) -> Unit) {
        try {
            val guardianMap = guardian.toMap()
            firestore.collection("guardians")
                .document(guardian.guardianID.toString())
                .set(guardianMap, SetOptions.merge())
                .addOnSuccessListener {
                    callback(true, "Guardian synced successfully.")
                }
                .addOnFailureListener { e ->
                    callback(false, "Error syncing guardian: ${e.message}")
                }
        } catch (e: Exception) {
            callback(false, "Exception during sync: ${e.message}")
        }
    }

    // Helper function to convert Guardian to Map
    private fun Guardian.toMap(): Map<String, Any?> {
        return mapOf(
            "Gname" to Gname,
            "number" to number,
            "CNIC" to CNIC,
            "Email" to Email,
            "QRcodeData" to QRcodeData,
            "QRcodeBase64" to QRcodeBase64,
            "studentDocumentID" to studentDocumentID,
            "userId" to userId
        );
    }

    // Helper function to convert Student to Map
    private fun Student.toMap(): Map<String, Any?> {
        return mapOf(
            "Sname" to Sname,
            "reg" to reg,
            "studentClass" to studentClass,
            "section" to section,
            "studentDocId" to studentDocId,
            "userId" to userId
        );
    }

}
