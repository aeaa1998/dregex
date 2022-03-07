package dregex

import algos.RegexVisitor
import extension.addAllUnique
import extension.plus
import utils.Constants
import utils.Identifiable
import kotlin.properties.Delegates

object RegexLeafProvider{
    var id = 1
}

abstract class RegexExpression(
    val expression: String
) : Identifiable<Int> {
    var left: RegexExpression? = null
    var right: RegexExpression? = null
    var completed: Boolean = false
    abstract fun setNode(reg: RegexExpression)
    abstract fun setComputedProperties()

    var isNullable by Delegates.notNull<Boolean>()
    var firstPos : MutableList<RegexExpression> = mutableListOf()
    var lastPos : MutableList<RegexExpression> = mutableListOf()
    var followPos : MutableList<RegexExpression> = mutableListOf()

    val isLeaf: Boolean
    get() = id > -1

    final override var id: Int = -1

    val isLeftInitialized: Boolean
    get() = left != null

    abstract fun accept(regexVisitor: RegexVisitor)


}

open class WordNode(expression: String) : RegexExpression(expression) {
    init {
        completed = true
        id = RegexLeafProvider.id
        RegexLeafProvider.id++
    }

    override fun setNode(reg: RegexExpression) {
        throw Exception("A word node cannot have children they are intended to be leafs")
    }

    override fun setComputedProperties() {
        isNullable = expression == Constants.clean
        firstPos = mutableListOf<RegexExpression>(this)
        lastPos = mutableListOf<RegexExpression>(this)
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
    override fun setComputedProperties() {
        isNullable = true
        //Set first pos
        left?.setComputedProperties()

        firstPos = left?.firstPos ?: mutableListOf()
        lastPos = left?.lastPos ?: mutableListOf()
        //
        lastPos.forEach { lastPos ->
            lastPos.followPos.addAllUnique(firstPos)
        }
    }

    override fun accept(regexVisitor: RegexVisitor) {
        //Just visit the node
        regexVisitor.visit(this)
    }
}
class OneOrMoreOperatorNode() : SingleOperatorNode("+"){
    override fun setComputedProperties() {
        left?.setComputedProperties()
        isNullable = left?.isNullable == true
        //Set the first pos
        firstPos = left?.firstPos ?: mutableListOf()
        lastPos = left?.lastPos ?: mutableListOf()
        //
        lastPos.forEach { lastPos ->
            lastPos.followPos.addAllUnique(firstPos)
        }
    }


    override fun accept(regexVisitor: RegexVisitor) {
        //Just visit the node
        regexVisitor.visit(this)
    }
}
class ZeroOrOne() : SingleOperatorNode("?"){
    override fun setComputedProperties() {
        isNullable = true
        //Set first pos
        left?.setComputedProperties()
        firstPos = left?.firstPos ?: mutableListOf()
        lastPos = left?.lastPos ?: mutableListOf()
        lastPos.forEach { lastPos ->
            lastPos.followPos.addAllUnique(firstPos)
        }
    }

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
        RegexOperators.Or -> OrOperatorNode()
        RegexOperators.Concat -> ConcatNode()
    }
}


class BruteForceEndNode() : WordNode("#"){
    override fun accept(regexVisitor: RegexVisitor) {
        //Just visit the node
//        regexVisitor.visit(this)
    }
    init {
        id = RegexLeafProvider.id
        RegexLeafProvider.id++
    }
}
class OrOperatorNode() : OperatorNode("|"){

    override fun setComputedProperties() {
        val left = this.left
        val right = this.right
        checkNotNull(right)
        checkNotNull(left)
        left.setComputedProperties()
        right.setComputedProperties()
        isNullable = left.isNullable || right.isNullable
        //Setting first pos
        firstPos = left.firstPos.plus(right.firstPos)
        //Setting last pos
        lastPos = left.lastPos.plus(right.lastPos)
    }

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

open class ConcatNode() : OperatorNode("CONCAT") {
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

    override fun setComputedProperties() {
        //Setting the nullable property
        val left = this.left
        val right = this.right
        checkNotNull(right)
        checkNotNull(left)
        left.setComputedProperties()
        right.setComputedProperties()
        isNullable = left.isNullable && right.isNullable
        //Set first pos
        firstPos = if (!left.isNullable){
            left.firstPos
        }else{
            left.firstPos.plus(right.firstPos)
        }
        //Set last pos
        lastPos = if (right.isNullable){
            left.lastPos.plus(right.lastPos)
        }else{
            right.lastPos
        }


        left.lastPos.forEach { lastPos ->
            lastPos.followPos.addAllUnique(right.firstPos)
        }
    }
}