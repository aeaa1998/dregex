package dregex

import algos.RegexVisitor
import automatons.NFA
import automatons.State


abstract class RegexExpression(
    val expression: String
){
    var left: RegexExpression? = null
    var right: RegexExpression? = null
    var completed: Boolean = false
    abstract fun setNode(reg: RegexExpression)

    val isLeftInitialized: Boolean
    get() = left != null

    abstract fun accept(regexVisitor: RegexVisitor)
}

class WordNode(expression: String) : RegexExpression(expression) {
    init {
        completed = true

    }
    override fun setNode(reg: RegexExpression) {

    }

    override fun accept(regexVisitor: RegexVisitor) {
        //Just visit the node
        regexVisitor.visit(this)
    }
}

object SingleOperatorNodeFactory {
    fun create(value: String) : SingleOperatorNode = when(RegexSingleOperators.getFromValue(value)){
        RegexSingleOperators.OneOrMore -> OneOrMoreOperatorNode()
        RegexSingleOperators.ZeroOrMore -> ZeroOrMoreOperatorNode()
        RegexSingleOperators.OneOrZero -> ZeroOrOne()
    }
}

class ZeroOrMoreOperatorNode() : SingleOperatorNode("*"){
    override fun accept(regexVisitor: RegexVisitor) {
        //Just visit the node
        regexVisitor.visit(this)
    }
}
class OneOrMoreOperatorNode() : SingleOperatorNode("+"){
    override fun accept(regexVisitor: RegexVisitor) {
        //Just visit the node
        regexVisitor.visit(this)
    }
}
class ZeroOrOne() : SingleOperatorNode("?"){
    override fun accept(regexVisitor: RegexVisitor) {
        //Just visit the node
        regexVisitor.visit(this)
    }
}

abstract class SingleOperatorNode(expression: String) : RegexExpression(expression) {
    override fun setNode(reg: RegexExpression) {
        left = reg
        completed = true
    }
}

object OperatorNodeFactory {
    fun create(value: String) : OperatorNode = when(RegexOperators.getFromValue(value)){
        RegexOperators.WildCard -> WildCardOperatorNode()
        RegexOperators.Or -> OrOperatorNode()
    }
}


class WildCardOperatorNode() : OperatorNode("."){
    override fun accept(regexVisitor: RegexVisitor) {
        //Just visit the node
        regexVisitor.visit(this)
    }
}
class OrOperatorNode() : OperatorNode("|"){
    override fun accept(regexVisitor: RegexVisitor) {
        //Just visit the node
        regexVisitor.visit(this)
    }
}

abstract class OperatorNode(expression: String) : RegexExpression(expression) {
    override fun setNode(reg: RegexExpression) {
        if (!isLeftInitialized){
            left = reg
        }else{
            right = reg
            completed = true
        }
    }
}

class ConcatNode() : RegexExpression("CONCAT") {
    override fun setNode(reg: RegexExpression) {
        if (!isLeftInitialized){
            left = reg
        }else{
            right = reg
            completed = true
        }
    }
    override fun accept(regexVisitor: RegexVisitor) {
        //Just visit the node
        regexVisitor.visit(this)
    }
}