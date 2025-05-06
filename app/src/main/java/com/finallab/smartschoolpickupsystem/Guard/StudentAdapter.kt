package com.finallab.smartschoolpickupsystem.Guard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.finallab.smartschoolpickupsystem.R

class StudentAdapter(
    private val students: List<Student>,
    private val context: Context
) : RecyclerView.Adapter<StudentAdapter.StudentViewHolder>() {

    inner class StudentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val studentName: TextView = itemView.findViewById(R.id.studentName)
        private val studentRegNo: TextView = itemView.findViewById(R.id.studentRegNo)
        private val studentClass: TextView = itemView.findViewById(R.id.studentClass)
        private val studentSection: TextView = itemView.findViewById(R.id.studentSection)

        fun bind(student: Student) {
            studentName.text = student.Sname
            studentRegNo.text = "Reg No: ${student.reg}"
            studentClass.text = "Class: ${student.studentClass}"
            studentSection.text = "Section: ${student.section}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.student_itemguard, parent, false)
        return StudentViewHolder(view)
    }

    override fun onBindViewHolder(holder: StudentViewHolder, position: Int) {
        holder.bind(students[position])
    }

    override fun getItemCount(): Int = students.size
}
