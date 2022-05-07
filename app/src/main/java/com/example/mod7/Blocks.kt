package com.example.mod7

//interface Block{ а ссылок с указателями нэма
//    var next:Block?; var prev:Block?;
//    fun execute()
//}

class VariableCreationBlock(pos: Pair<Double, Double>){
    var variableName = ""
    var initExpr: Expr? = null
    init{

    }
}

class VariableAssignmentBlock(pos: Pair<Double, Double>){
    init{

    }
}

class CringeBlock(pos: Pair<Double, Double>){
    init{

    }
}