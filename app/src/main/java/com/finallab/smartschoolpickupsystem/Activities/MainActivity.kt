package com.finallab.smartschoolpickupsystem.Activities

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.finallab.smartschoolpickupsystem.DataModels.Student
import com.finallab.smartschoolpickupsystem.Recycler.OnItemDeletedListener
import com.finallab.smartschoolpickupsystem.Recycler.RecyclerViewAdapter
import com.finallab.smartschoolpickupsystem.Room.AppDatabase
import com.finallab.smartschoolpickupsystem.Utilities
import com.finallab.smartschoolpickupsystem.ViewModel.GuardianStudentViewModel
import com.finallab.smartschoolpickupsystem.ViewModel.GuardianStudentViewModelFactory
import com.finallab.smartschoolpickupsystem.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.core.widget.doOnTextChanged


class MainActivity : AppCompatActivity(), OnItemDeletedListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: RecyclerViewAdapter
    private lateinit var viewModel: GuardianStudentViewModel
    private val auth = FirebaseAuth.getInstance()
    private lateinit var searchBar: AutoCompleteTextView
    private var fullStudentList: List<Student> = listOf()

    private val addStudentLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            if (Utilities.isNetworkConnected(this)) {
                syncAllData()
            } else {
                loadStudentDataOffline()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()

        viewModel = GuardianStudentViewModelFactory(
            com.finallab.smartschoolpickupsystem.model.Repository.GuardianStudentRepository(
                AppDatabase.getDatabase(this)
            )
        ).create(GuardianStudentViewModel::class.java)

        if (Utilities.isNetworkConnected(this)) {
            syncAllData()
        } else {
            Utilities.showNotConnectedSnack(binding.root, this)
            loadStudentDataOffline()
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            if (Utilities.isNetworkConnected(this)) {
                syncAllData()
            } else {
                Utilities.showNotConnectedSnack(binding.root, this)
                loadStudentDataOffline()
            }

            // Important! Stop the refreshing spinner
            binding.swipeRefreshLayout.isRefreshing = false
        }
        searchBar = binding.searchBar

        searchBar.doOnTextChanged { text, _, _, _ ->
            val query = text?.toString()?.trim()?.lowercase() ?: ""
            applySearchFilter(query)
            if (query.isEmpty()) unfocusSearchBar()

        }
    }
    private fun applySearchFilter(query: String) {
        val filtered = fullStudentList.filter { student ->
            student.Sname.lowercase().contains(query) ||
                    student.studentClass.lowercase().contains(query) ||
                    student.section.lowercase().contains(query)
        }
        adapter.updateData(filtered.toMutableList())
    }
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()

                    binding.searchBarLayout.isHintEnabled = false
                    binding.searchBarLayout.isHintEnabled = true

                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.windowToken, 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
    private fun unfocusSearchBar() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
        binding.searchBar.clearFocus()
    }

    private fun setupRecyclerView() {
        adapter = RecyclerViewAdapter(
            items = mutableListOf(),
            lifecycleScope = lifecycleScope,
            listener = this
        )
        binding.recyclerview.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.addS.setOnClickListener {
            val intent = Intent(this, AddStudentActivity::class.java)
            addStudentLauncher.launch(intent)
        }

        binding.backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun syncAllData() {
        binding.progressBar.visibility = View.VISIBLE

        viewModel.syncAllData()

        lifecycleScope.launch {
            delay(1500) // Small delay to ensure Room is updated after Firestore
            loadStudentDataOffline()
            showToast("Synced Successfully!")
        }
    }

    private fun loadStudentDataOffline() {
        val userId = auth.currentUser?.uid ?: run {
            showToast("User not logged in")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val students = AppDatabase.getDatabase(this@MainActivity)
                .studentDao()
                .getStudentsByUserId(userId)

            withContext(Dispatchers.Main) {
                if (students.isNotEmpty()) {
                    updateRecyclerView(students)
                } else {
                    showEmptyState()
                }
            }
        }
    }
    private fun updateRecyclerView(students: List<Student>) {
        fullStudentList = students
        applySearchFilter(binding.searchBar.text.toString().trim().lowercase())
        binding.progressBar.visibility = View.GONE
        binding.emptyStateView.visibility = if (students.isEmpty()) View.VISIBLE else View.GONE
    }


    private fun showEmptyState() {
        binding.progressBar.visibility = View.GONE
        binding.emptyStateView.visibility = View.VISIBLE
        adapter.updateData(mutableListOf())
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDataUpdated() {
        if (Utilities.isNetworkConnected(this)) {
            syncAllData()
        } else {
            Utilities.showNotConnectedSnack(binding.root, this)
            loadStudentDataOffline()
            showToast("Loaded from Cache")
        }
    }

}
