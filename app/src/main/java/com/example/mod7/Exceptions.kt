package com.example.mod7

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import com.example.mod7.Error
import com.example.mod7.databinding.ActivityMainBinding

const val EXC_MSG = "EXCEPTION_MSG"

class ExceptionHandler(program: Program, binding: ActivityMainBinding) {
    private val program = program
    private val binding: ActivityMainBinding = binding
    fun throwRuntime(
        msg: String,
        context: Context = binding.rootLayout.context,
        showLine: Boolean = true
    ) {
        program.stopExecution()
        val intent: Intent = Intent(context, Error::class.java)
        intent.putExtra(
            EXC_MSG,
            "${if (showLine) program.getCurrentLine().toString() + ": " else ""}$msg"
        );
        context.startActivity(intent)
    }

    fun throwCritical(msg: String) {
        throw Exception(msg)
    }
}