package com.finallab.smartschoolpickupsystem.Guard

import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.finallab.smartschoolpickupsystem.databinding.CustomToastBinding
import com.finallab.smartschoolpickupsystem.databinding.SuccessToastBinding


object ToastUtil {

    enum class ToastType {
        SUCCESS,
        ERROR
    }

    fun showToast(context: Context, message: String, type: ToastType = ToastType.ERROR) {
        val inflater = LayoutInflater.from(context)
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT

        val toastView = when (type) {
            ToastType.SUCCESS -> {
                val successBinding = SuccessToastBinding.inflate(inflater)
                successBinding.success.text = message
                successBinding.root
            }
            ToastType.ERROR -> {
                val errorBinding = CustomToastBinding.inflate(inflater)
                errorBinding.fail.text = message
                errorBinding.root
            }
        }

        toast.view = toastView
        toast.show()
    }
}
