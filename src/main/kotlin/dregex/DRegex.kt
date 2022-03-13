package dregex

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.layout.mxIGraphLayout
import com.mxgraph.model.mxICell
import com.mxgraph.util.mxCellRenderer
import de.vandermeer.asciitable.AsciiTable
import graphs.RegexEdge
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DefaultDirectedGraph
import utils.Constants
import java.awt.Color
import java.io.File
import java.util.*
import javax.imageio.ImageIO


class DRegex(
    private val regexString: String
) {
    lateinit var expression : RegexExpression
    private val singleOperators: List<String> = listOf("+", "*", "?")

    private fun precedence(char: Char): Int {
        return when (char) {
            '|' -> 1
            '•' -> return 2
            '*', '?', '+' -> return 3
            else -> (-1)
        }
    }

    private fun precedence(char: String): Int {
        return when (char) {
            "|" -> 1
            Constants.concat -> return 2
            "*", "?", "+" -> return 3
            else -> (-1)
        }
    }

    private fun isOperator(ch: Char): Boolean {
        return ch == '•' || ch == '|' || ch == '?' || ch == '*' || ch == '+'
    }

    private fun isOperator(ch: String): Boolean {
        return ch == Constants.concat || ch == "|" || ch == "?" || ch == "*" || ch == "+"
    }


    private fun isParenthesis(ch: Char): Boolean {
        return ch == '(' || ch == ')'
    }

    private fun isParenthesis(ch: String): Boolean {
        return ch == "(" || ch == ")"
    }


    private fun expressionTree(postfix: String): RegexExpression {
        val st: Stack<RegexExpression> = Stack<RegexExpression>()

        for (i in postfix.indices) {
            val letter = postfix[i].toString()
            if (isOperator(postfix[i])) {
                val nodeOp: RegexExpression
                when  {
                    //If it is a single operator we only pop one element from our stack and assign it
                    singleOperators.contains(letter) -> {
                        nodeOp = SingleOperatorNodeFactory.create(letter)
                        val a = st.pop()
                        nodeOp.setNode(a)
                    }
                    else -> {
                        nodeOp = OperatorNodeFactory.create(letter)
                        val right = st.pop()
                        val left = st.pop()
                        nodeOp.setNode(left)
                        nodeOp.setNode(right)
                    }
                }

                st.push(nodeOp)
            } else {
                st.push(WordNode(letter))
            }
        }
        return st.pop()
    }

    private fun expressionTreeList(postfix: List<String>): RegexExpression {
        val st: Stack<RegexExpression> = Stack<RegexExpression>()

        for (i in postfix.indices) {
            val letter = postfix[i]
            if (isOperator(postfix[i])) {
                val nodeOp: RegexExpression
                when  {
                    //If it is a single operator we only pop one element from our stack and assign it
                    singleOperators.contains(letter) -> {
                        nodeOp = SingleOperatorNodeFactory.create(letter)
                        val a = st.pop()
                        nodeOp.setNode(a)
                    }
                    else -> {
                        nodeOp = OperatorNodeFactory.create(letter)
                        val right = st.pop()
                        val left = st.pop()
                        nodeOp.setNode(left)
                        nodeOp.setNode(right)
                    }
                }

                st.push(nodeOp)
            } else {
                st.push(WordNode(letter))
            }
        }
        return st.pop()
    }

    private fun infixToPostfix(exp: String): String {
        var result = ""
        val stack = Stack<Char>()
        for (element in exp) {
            when {
                !isOperator(element) && !isParenthesis(element) -> result += element
                element == '(' -> stack.push(element)
                element == ')' -> {
                    while (!stack.isEmpty() &&
                        stack.peek() != '('
                    ) result += stack.pop()
                    stack.pop()
                }
                else -> {
                    val cPrec = precedence(element)
                    while (!stack.isEmpty() && cPrec <= precedence(stack.peek())
                    ) {
                        result += stack.pop()
                    }
                    stack.push(element)
                }
            }
        }


        while (!stack.isEmpty()) {
            if (stack.peek() == '(') throw Exception("No se cerro parentesis")
            result += stack.pop()
        }
        return result
    }

    private fun infixToPostfixList(exp: List<String>): List<String> {
        val result = mutableListOf<String>()
        val stack = Stack<String>()
        for (element in exp) {
            when {
                !isOperator(element) && !isParenthesis(element) -> result.add(element)
                element == "(" -> stack.push(element)
                element == ")" -> {
                    while (!stack.isEmpty() &&
                        stack.peek() != "("
                    ) result.add(stack.pop())
                    stack.pop()
                }
                else -> {
                    val cPrec = precedence(element)
                    while (!stack.isEmpty() && cPrec <= precedence(stack.peek())
                    ) {
                        result.add(stack.pop())
                    }
                    stack.push(element)
                }
            }
        }


        while (!stack.isEmpty()) {
            if (stack.peek() == "(") throw Exception("No se cerro parentesis")
            result.add(stack.pop())
        }
        return result
    }


    private fun normalizeStack(): String {
        var normalizedString = ""
        regexString.forEachIndexed { index, char ->
            val validStart = (char == '(' || char == '|').not()
            val validFinish =
                if (index < regexString.length - 1) (regexString[index+1] == ')' || isOperator(regexString[index+1])).not() else false
            normalizedString += char
            if (validStart && validFinish) {
                normalizedString += "•"
            }
        }

        return infixToPostfix(normalizedString)

    }

    private fun normalizeStackList(): List<String> {
        val stringsList = mutableListOf<String>()
        regexString.forEachIndexed { index, char ->
            val validStart = (char == '(' || char == '|').not()
            val validFinish =
                if (index < regexString.length - 1) (regexString[index+1] == ')' || isOperator(regexString[index+1])).not() else false
            stringsList.add(char.toString())
            if (validStart && validFinish) {
                stringsList.add(Constants.concat)
            }
        }

        return infixToPostfixList(stringsList)

    }


    fun build() : RegexExpression {
//        expression =  expressionTree(normalizeStack())
        expression = expressionTreeList(normalizeStackList())
        buildGraph()
        return expression
    }

    fun printDescription() {
        val at = AsciiTable()
        at.addRule()
        at.addRow("Nombre en el arbol", "Id (solo si es hoja)", "lastPos", "firstPos", "followPos")
        at.addRule()
        expression.printTable(at)
        println(at.render())
    }

    fun getDirectExpression() : DirectRegex {
        if (!this::expression.isInitialized){
            build()
        }
        return DirectRegex(this@DRegex.expression)

    }

    fun buildGraph(){
        val directedGraph = DefaultDirectedGraph<String, RegexEdge>(RegexEdge::class.java)
        expression.setToNodeGraph(directedGraph)


        val imgFile = File("src/main/kotlin/outputs/tree/${regexString}.png")
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