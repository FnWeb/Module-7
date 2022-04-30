package com.example.mod7

import kotlin.Exception

class Program{
    val uninitializedBool: BoolType? = null; val uninitializedInteger: IntegerType? = null; val uninitializedDouble: DoubleType? = null
    val typeNameBool = "Bool"; val typeNameInteger = "Int"; val typeNameDouble = "Double"
    private var variables: MutableMap<String, Variable?> = mutableMapOf()
    public fun get(variable: String): Variable {
        if(!variables.containsKey(variable)){
            throw Exception("Accessing unknown variable $variable")
        }
        return variables[variable]?:throw Exception("Accessing uninitialized variable $variable")
    }
    public fun createVariable(variable: String, type: String, init: Any): Variable{
        if(variables.containsKey(variable)){
            throw Exception("Attempting to create existing variable $variable")
        }
        variables[variable] = when(type){
            typeNameInteger -> IntegerType(init)
            typeNameBool -> BoolType(init)
            typeNameDouble -> DoubleType(init)
            else -> throw Exception("Unable to create variable: unknown type $type")
        }
        return variables[variable]?:throw Exception("Error creating variable $variable")
    }
    public fun createVariable(variable: String, type: String){
        if(variables.containsKey(variable)){
            throw Exception("Attempting to create existing variable $variable")
        }
        variables[variable] = when(type){
            typeNameInteger -> uninitializedInteger
            typeNameBool -> uninitializedBool
            typeNameDouble -> uninitializedDouble
            else -> throw Exception("Unable to create variable: unknown type $type")
        }
    }
    public fun setValue(variable: String, value: Any): Variable{
        if(variables.containsKey(variable)){
            throw Exception("Unable to set variable $variable - doesn't exist")
        }
        if(variables[variable] != null){
            variables[variable]?.set(value)
            return variables[variable]?:throw Exception("Error assigning $variable (is ${variables[variable]}) to $value")
        }

        variables[variable] = when(variables[variable]){
            is IntegerType -> IntegerType(value)
            is BoolType -> BoolType(value)
            is DoubleType -> DoubleType(value)
            else -> throw Exception("Unable to assign $variable - unknown type")
        }
        return variables[variable]?:throw Exception("Error assigning $variable (is ${variables[variable]}) to $value")
    }

}