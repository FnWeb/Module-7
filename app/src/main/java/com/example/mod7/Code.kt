package com.example.mod7

import android.content.Context
import android.app.Application
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.mod7.databinding.ActivityMainBinding
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.selects.select
import java.lang.NullPointerException
import java.util.*
import kotlin.Exception
import kotlin.coroutines.coroutineContext
import kotlin.concurrent.timerTask

const val typeNameBool = "Bool";
const val typeNameInteger = "Int";
const val typeNameDouble = "Double"

class Program(binding: ActivityMainBinding) {
    private var timer: Timer = Timer()
    val exceptionHandler = ExceptionHandler(this, binding)
    val blockViewManager = BlockViewManager(binding)
    private var isRunning = false
    private var variables: MutableMap<String, Variable> = mutableMapOf()
    private var binding: ActivityMainBinding = binding
    private var iterationCounter = 0
    private var consoleOutput = ""

    fun getVariableMap(): Map<String, Variable?> {
        return variables.toMap() //readonly
    }

    fun deleteVariable(variable: String) {
        variables.remove(variable)
    }

    fun get(variable: String): Variable {
        if (!variables.containsKey(variable)) {
            throw Exception("Accessing unknown variable $variable")
        }
        return variables[variable] ?: throw Exception("Accessing uninitialized variable $variable")
    }

    fun createVariable(variable: String, type: String, init: Any): Variable {
        if (variables.containsKey(variable)) {
            exceptionHandler.throwRuntime("Attempting to create existing variable $variable")
        }
        if (listOf<String>(typeNameInteger, typeNameBool, typeNameDouble).indexOf(type) < 0) {
            exceptionHandler.throwRuntime("Unable to create variable: unknown type $type")
        }
        variables[variable] = when (type) {
            typeNameInteger -> IntegerType(init)
            typeNameBool -> BoolType(init)
            typeNameDouble -> DoubleType(init)
            else -> IntegerType(0)
        }
        if (variables[variable] == null) {
            exceptionHandler.throwCritical("Error creating variable $variable")
        }
        return variables[variable]!!
    }

    public fun createVariable(variable: String, type: String) {
        if (variables.containsKey(variable) || variable.length <= 0) {
            throw Exception("Attempting to create existing variable [$variable]")
        }
        variables[variable] = when (type) {
            typeNameInteger -> IntegerType(0)
            typeNameBool -> BoolType(0)
            typeNameDouble -> DoubleType(0)
            else -> throw Exception("Unable to create variable: unknown type $type")
        }
    }

    fun isProgramRunning(): Boolean {
        return isRunning
    }

    private var instructions: LinkedList<String> = LinkedList<String>()
    fun appendInstruction(
        context: Context,
        layoutInflater: LayoutInflater,
        parentBlock: Int,
        type: String
    ) {
        if (!(type in listOf(BLOCK_TYPE_ASSIGN, BLOCK_TYPE_VAR, BLOCK_TYPE_PRINT))) {
            exceptionHandler.throwCritical("Unknown block type $type")
        }
        instructions.add(type)
        blockViewManager.addBlock(context, layoutInflater, parentBlock, instructions.size, type)
    }

    fun moveInstructionUp() {
        if (instructions.size <= 1 || selectedBlock <= 0 || selectedBlock >= instructions.size) {
            exceptionHandler.throwRuntime(
                "Unable to move the block #$selectedBlock Up",
                binding.rootLayout.context
            )
            return
        }
        blockViewManager.swapBlocks(selectedBlock, selectedBlock - 1);
        Collections.swap(instructions, selectedBlock, --selectedBlock)
    }

    fun moveInstructionDown() {
        if (instructions.size <= 1 || selectedBlock < 0 || selectedBlock >= instructions.size - 1) {
            exceptionHandler.throwRuntime(
                "Unable to move the block #$selectedBlock Up",
                binding.rootLayout.context
            )
            return
        }
        blockViewManager.swapBlocks(selectedBlock, selectedBlock + 1);
        Collections.swap(instructions, selectedBlock, ++selectedBlock)
    }

    fun printToConsole(tag: String = "", msg: String = "\n") {
        consoleOutput += "$tag$msg\n"
    }

    fun getCurrentLine(): Int {
        return iterationCounter
    }

    fun stopExecution() {
        timer.cancel()
        timer.purge()
        isRunning = false
        selectedBlock = -1
    }

    fun startExecution() {
        if (isRunning) {
            return
        }
        if (instructions.size < 1) {
            exceptionHandler.throwRuntime("Instruction pool empty or corrupted")
        }
        isRunning = true
        iterationCounter = 0
        variables.clear()
        timer = Timer()
        timer.schedule(
            timerTask {
                if (iterationCounter < instructions.size) {
                    runInstructionSet()
                    blockViewManager.selectBlock(iterationCounter)
                } else {
                    stopExecution()
                    consoleOutput += "---------------------------------------\n"
                    exceptionHandler.throwRuntime(consoleOutput, showLine = false)
                }
            },
            0,
            (1000 / (kotlin.math.min(kotlin.math.max(programSpeed, 1), 10) * 100)).toLong()
        )
    }

    private fun runInstructionSet() {
        when (instructions[iterationCounter]) {
            BLOCK_TYPE_VAR -> {
                val contents =
                    blockViewManager.blocks[iterationCounter].first.findViewById<EditText>(R.id.variableNameTextView).text.toString()
                        .split(
                            variableNameRegex, ","
                        ) as List<String>
                contents.forEach {
                    blockViewManager.blocks[iterationCounter].first.findViewById<Spinner>(R.id.spinner).selectedItem.apply {
                        if (it.length <= 0) {
                            exceptionHandler.throwRuntime("Error creating variable: Bad name")
                        }
                        if (equals("Integer")) {
                            createVariable(it, typeNameInteger)
                        } else if (equals("Bool")) {
                            createVariable(it, typeNameBool)
                        } else if (equals("Double")) {
                            createVariable(it, typeNameDouble)
                        }
                    }
                }
                printToConsole(
                    "Variable${if (contents.size > 1) "s" else ""} [${
                        contents.joinToString(
                            ", "
                        )
                    }] created"
                )
            }
            BLOCK_TYPE_ASSIGN -> {
                val variableName =
                    blockViewManager.blocks[iterationCounter].first.findViewById<EditText>(R.id.variableNameTextView).text.toString()
                val input =
                    blockViewManager.blocks[iterationCounter].first.findViewById<EditText>(R.id.expressionField).text.toString()
                val value = parseRPN(
                    if (input.isNotEmpty() && input[0] == '-') {
                        "0$input"
                    } else
                        input
                )
                if (variables[variableName] is IntegerType) {
                    variables[variableName] = IntegerType(value)
                } else if (variables[variableName] is BoolType) {
                    variables[variableName] = BoolType(value)
                } else if (variables[variableName] is DoubleType) {
                    variables[variableName] = DoubleType(value)
                }
                variables[variableName]?.initialized = true
                printToConsole(
                    "",
                    "Variable $variableName is assigned ${
                        variables[variableName]?.invoke().toString()
                    }"
                )


            }
            BLOCK_TYPE_PRINT -> {
                blockViewManager.blocks[iterationCounter].first.apply {
                    printToConsole(
                        "",
                        parseRPN(findViewById<EditText>(R.id.expressionField).text.toString())
                    )
                }
            }
        }
        iterationCounter++
    }

    fun clearInstructions() {
        stopExecution()
        instructions.clear()
        blockViewManager.clearBlocks()
        consoleOutput = ""
    }

    fun clearConsoleOutput() {
        consoleOutput = ""
    }

    fun precedence(operator: Char): Int {
        return when (operator) {
            '*' -> 0
            '/' -> 0
            '%' -> 0
            '+' -> 1
            '-' -> 1
            else -> 100
        }
    }

    fun parseRPN(input: String): String {
        var index = 0
        val numberRegex = "-?[0-9]+(\\.[0-9]+)?".toRegex()
        val operatorRegex = "[%/*+-]".toRegex()
        var token: String
        var output: Queue<String> = LinkedList<String>()// Kotlin moment
        var operators: Stack<Char> = Stack()
        while (index < input.length) {// Цикл невозможно реализовать через for a in b, так как a неизменяемое и в таком случае потребуется куда больше заморочек со считыванием числа через continue
            // Но штраф за неиспользование for in нам все равно впаяют))0)
            token = "" + input[index]
            if (token.matches(numberRegex)) {
                while (index + 1 < input.length && ((token + input[index + 1]).matches(numberRegex) || index + 2 < input.length && input[index + 1] == '.' && (token + input[index + 1] + input[index + 2]).matches(
                        numberRegex
                    ))
                ) {
                    token += input[++index]
                }
                output.add(token)
            } else if (token.matches("[a-zA-Z]".toRegex())) {
                while (index + 1 < input.length && (token + input[index + 1]).matches(
                        variableNameRegex.toRegex()
                    )
                ) {
                    token += input[++index]
                }
                output.add(
                    if (token == "true" || token == "false") (token == "true").toString() else variables[token]?.invoke()
                        .toString()
                )
                if (token != "true" && token != "false" && (!variables.containsKey(token) || !(variables[token]?.initialized
                        ?: throw Exception("Variable $token doesn't exist")))
                ) {
                    exceptionHandler.throwRuntime("Variable $token is uninitialized or does not exist")
                }
            } else if (token.matches(operatorRegex)) {
                while (operators.isNotEmpty() && precedence(operators.peek()) <= precedence(token[0]) /*||token is left-associative && equal precedence*/) {
                    output.add(operators.pop().toString())
                }
                operators.push(token[0])
            } else if (token == "(") {
                operators.push(token[0])
            } else if (token == ")") {
                while (operators.isNotEmpty() && operators.peek() != '(') {
                    output.add(operators.pop().toString())
                }
                if (operators.isEmpty()) {
                    exceptionHandler.throwRuntime("RPN: Parenthesis Mismatch ${output.toString()} ( expected for $input")
                }
                operators.pop()
                if (false) {//todo operators.peek() is a function
                    output.add(operators.pop().toString())
                }
            }
            index++
        }

        while (operators.isNotEmpty()) {
            if (operators.peek() == '(')
                exceptionHandler.throwRuntime("RPN: Parenthesis Mismatch ${output.toString()} unresolved ( for $input")
            output.add(operators.pop().toString())
        }

        var operands: Stack<String> = Stack()
        var right: String = ""
        var left: String = ""
        var result: String = ""
        while (output.isNotEmpty()) {
            token = output.remove()
            if (token.matches(operatorRegex)) {
                right = operands.pop()
                left = operands.pop()
                if (token == "%" && (right.indexOf(".") >= 0 || left.indexOf(".") >= 0)) {
                    exceptionHandler.throwRuntime("Unable to calculate floating point number mod")
                }
                Log.wtf("wtf", "$left $token $right")
                result = when (token) {
                    "+" -> {
                        if (right.indexOf(".") >= 0 || left.indexOf(".") >= 0) {
//                            (left.toDouble()+right.toDouble()).toString()
                            (DoubleType(left) + DoubleType(right)).invoke().toString()
                        } else {
                            (IntegerType(left) + IntegerType(right)).invoke().toString()
                        }
                    }
                    "-" -> {
                        if (right.indexOf(".") >= 0 || left.indexOf(".") >= 0) {
                            (DoubleType(left) - DoubleType(right)).invoke().toString()
                        } else {
                            (IntegerType(left) - IntegerType(right)).invoke().toString()
                        }
                    }
                    "*" -> {
                        if (right.indexOf(".") >= 0 || left.indexOf(".") >= 0) {
                            (DoubleType(left) * DoubleType(right)).invoke().toString()
                        } else {
                            (IntegerType(left) * IntegerType(right)).invoke().toString()
                        }
                    }
                    "/" -> {
                        if (right.indexOf(".") >= 0 || left.indexOf(".") >= 0 || left.toInt() % right.toInt() > 0) {
                            (DoubleType(left) / DoubleType(right)).invoke().toString()
                        } else {
                            (IntegerType(left) / IntegerType(right)).invoke().toString()
                        }
                    }
                    "%" -> {
                        (IntegerType(left) % IntegerType(right)).invoke().toString()
                    }
                    else -> throw Exception("Operator parsing error")
                }
                operands.push(result)
            } else {
                operands.push(token)
            }
        }
        return operands.first()
    }

}