package dregex

import algos.RegexVisitor
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.layout.mxIGraphLayout
import com.mxgraph.util.mxCellRenderer
import de.vandermeer.asciitable.AsciiTable
import extension.addAllUnique
import extension.plus
import graphs.RegexEdge
import kotlinx.serialization.Serializable
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DefaultDirectedGraph
import tokens.TokenExpression
import utils.Constants
import utils.Identifiable
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO
import kotlin.properties.Delegates

object RegexLeafProvider{
    var id = 1
    var id2 = 1
}



abstract class RegexExpression(
    val expression: String
) : Identifiable<Int>
{

    var left: RegexExpression? = null
    set(value) {
        field = value
    }

    var right: RegexExpression? = null
    set(value) {
        field = value
    }

    var completed: Boolean = false
    abstract fun setNode(reg: RegexExpression)
    abstract fun setComputedProperties()

    var isNullable by Delegates.notNull<Boolean>()
    var firstPos : Set<RegexExpression> = setOf()
    var lastPos : Set<RegexExpression> = setOf()
    var followPos : MutableSet<RegexExpression> = mutableSetOf()

    val isLeaf: Boolean
    get() = id > -1

    final override var id: Int = -1

    val id2: Int

    val isLeftInitialized: Boolean
    get() = left != null

    init {
        id2 = RegexLeafProvider.id2
        RegexLeafProvider.id2++
    }

    lateinit var tokenExpression: TokenExpression
    val inited: Boolean
    get() = this::tokenExpression.isInitialized

    abstract fun accept(regexVisitor: RegexVisitor)

    open fun propagate(tokenExpression: TokenExpression) {
        this.tokenExpression = tokenExpression
        left?.propagate(tokenExpression)
        right?.propagate(tokenExpression)
    }

    fun setToNodeGraph(directedGraph: DefaultDirectedGraph<String, RegexEdge>){
        directedGraph.addVertex("${expression}_$id2")
        left?.let {
            it.setToNodeGraph(directedGraph)
            directedGraph.addEdge("${expression}_$id2", "${it.expression}_${it.id2}")

        }

        right?.let {
            it.setToNodeGraph(directedGraph)
            directedGraph.addEdge("${expression}_$id2", "${it.expression}_${it.id2}")
        }

    }

    fun printTable(
        table: AsciiTable
    ){
        table.addRow(
            "${expression}_$id2",
            if (isLeaf) id else "",
            lastPos.map { it.id }.joinToString(),
            firstPos.map { it.id }.joinToString(),
            followPos.map { it.id }.joinToString()
        )
        table.addRule()
        left?.printTable(table)

        right?.printTable(table)
    }

    fun buildGraphFromE(){
        val directedGraph = DefaultDirectedGraph<String, RegexEdge>(RegexEdge::class.java)
        setToNodeGraph(directedGraph)


        val imgFile = File("src/main/kotlin/outputs/tree/${expression}.png")
        if (imgFile.exists()){
            imgFile.delete()
            imgFile.createNewFile()
        }else{
            imgFile.createNewFile()
        }
        val graphAdapter = JGraphXAdapter(directedGraph)

        val layout: mxIGraphLayout = mxHierarchicalLayout(graphAdapter)
        layout.execute(graphAdapter.defaultParent)

        val image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2.0, Color.WHITE, true, null)

        ImageIO.write(image, "PNG", imgFile)
    }


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
        firstPos = setOf<RegexExpression>(this)
        lastPos = setOf(this)
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

        firstPos = left?.firstPos ?: mutableSetOf()
        lastPos = left?.lastPos ?: mutableSetOf()
        //
        lastPos.forEach { lastPos ->
            lastPos.followPos.addAll(firstPos)
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
        firstPos = left?.firstPos ?: mutableSetOf()
        lastPos = left?.lastPos ?: mutableSetOf()
        //
        lastPos.forEach { lastPos ->
            lastPos.followPos.addAll(firstPos)
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
        firstPos = left?.firstPos ?: mutableSetOf()
        lastPos = left?.lastPos ?: mutableSetOf()
        lastPos.forEach { lastPos ->
            lastPos.followPos.addAll(firstPos)
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
        RegexOperators.TokenOr -> TokenOrOperatorNode()
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


open class OrOperatorNode(exp: String = "|") : OperatorNode(exp){

    override fun setComputedProperties() {
        val left = this.left
        val right = this.right
        checkNotNull(right)
        checkNotNull(left)
        left.setComputedProperties()
        right.setComputedProperties()
        isNullable = left.isNullable || right.isNullable
        //Setting first pos
        firstPos = left.firstPos + right.firstPos
        //Setting last pos
        lastPos = left.lastPos.plus(right.lastPos)
    }

    override fun accept(regexVisitor: RegexVisitor) {
        //Just visit the node
        regexVisitor.visit(this)
    }

}


class TokenOrGenerator(
    val expressions: List<TokenExpression>,
    val ignoreList: TokenExpression?
) {
    fun generate() : TokenOrOperatorNode {
        var p1: RegexExpression? = null
        var p2: RegexExpression? = null
        lateinit var tokenOrOperatorNode: TokenOrOperatorNode
        var initilized = false
        var expressions = expressions
        ignoreList?.let { expressions = expressions+it }
        expressions.forEachIndexed { index, regexExpression ->
            regexExpression.propagate(index)
            if (p1 == null){
                p1 = regexExpression.regexExpression
            }else if (p2 == null){
                p2 = regexExpression.regexExpression
                val t = TokenOrOperatorNode().apply {
                    left = p1
                    right = p2
                }
                initilized = true
                p1 = t
                p2 = null
                tokenOrOperatorNode = t
            }
        }

        if (p2 == null && !initilized){
            val t = TokenOrOperatorNode().apply {
                left = p1
                right = p1
            }
            tokenOrOperatorNode = t
        }

        return tokenOrOperatorNode
    }
}


class TokenOrOperatorNode() : OrOperatorNode("OR")



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



open class ConcatNode(expression: String = Constants.concat) : OperatorNode(expression) {
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
            lastPos.followPos.addAll(right.firstPos)
        }
    }
}