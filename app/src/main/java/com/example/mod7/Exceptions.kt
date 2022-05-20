package com.example.mod7

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import com.example.mod7.Error

const val EXC_MSG = "EXCEPTION_MSG"

class ExceptionHandler {
    fun throwRuntime(msg: String, context: Context){
        val intent: Intent = Intent(context, Error::class.java)
        intent.putExtra(EXC_MSG, msg);
        context.startActivity(intent)
    }
    fun throwCritical(msg: String){
        throw Exception(msg)
    }
}