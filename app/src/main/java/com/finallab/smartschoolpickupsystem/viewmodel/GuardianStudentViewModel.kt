package com.finallab.smartschoolpickupsystem.ViewModel

import androidx.lifecycle.*
import com.finallab.smartschoolpickupsystem.DataModels.Guardian
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.model.Repository.GuardianStudentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GuardianStudentViewModel(private val repository: GuardianStudentRepository) : ViewModel() {

    // ✅ StateFlow for Guardians
    val guardians: StateFlow<List<Guardian>> = repository.getAllGuardians()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ✅ StateFlow for Students
    val students: StateFlow<List<Student>> = repository.getAllStudents()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // ✅ Insert Guardian
    fun insertGuardian(guardian: Guardian) {
        viewModelScope.launch {
            repository.insertGuardian(guardian)
        }
    }

    // ✅ Delete Guardian
    fun deleteGuardian(guardian: Guardian) {
        viewModelScope.launch {
            repository.deleteGuardian(guardian)
        }
    }

    // ✅ Update Guardian
    fun updateGuardian(guardian: Guardian) {
        viewModelScope.launch {
            repository.updateGuardian(guardian)
        }
    }

    // ✅ Insert Student
    fun insertStudent(student: Student) {
        viewModelScope.launch {
            repository.insertStudent(student)
        }
    }

    // ✅ Delete Student
    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    // ✅ Update Student
    fun updateStudent(student: Student) {
        viewModelScope.launch {
            repository.updateStudent(student)
        }
    }

    // ✅ Sync Data from Firestore to Room
    fun syncDataFromFirestore() {
        viewModelScope.launch {
            repository.syncGuardiansFromFirestore()
            repository.syncStudentsFromFirestore()
        }
    }

    fun insertOrUpdateGuardian(guardian: Guardian) {
        viewModelScope.launch {
            repository.insertOrUpdateGuardian(guardian)
        }
    }

    fun insertOrUpdateStudent(student: Student) {
        viewModelScope.launch {
            repository.insertOrUpdateStudent(student)
        }
    }
    suspend fun getGuardiansByUserId(userId: String): List<Guardian> {
           return  repository.getGuardiansByUserId(userId)
    }
}


// ✅ ViewModel Factory (for constructor injection)
class GuardianStudentViewModelFactory(private val repository: GuardianStudentRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GuardianStudentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GuardianStudentViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
