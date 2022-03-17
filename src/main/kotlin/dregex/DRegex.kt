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

/**
 * DRegex
 * @constructor
 * @param regexString [String] The string where we can show our regex
 *
 * @property expression [RegexExpression]
 * @property singleOperators [List]
 */
class DRegex(
    private val regexString: String
) {
    lateinit var expression : RegexExpression
    private val singleOperators: List<String> = listOf("+", "*", "?")

    /**
     * This function is to check the precedence of the operator
     * @param char [String] The operator string
     * @return [Int] the precedence value
     */
    private fun precedence(char: String): Int {
        return when (char) {
            "|" -> 1
            Constants.concat -> return 2
            "*", "?", "+" -> return 3
            else -> (-1)
        }
    }

    /**
     * This function is to check if the val is a operator
     * @param ch [Char]
     * @return [Boolean] either it is or not a operator
     */
    private fun isOperator(ch: Char): Boolean {
        return ch == '|' || ch == '?' || ch == '*' || ch == '+'
    }


    /**
     * This function is to check if the val is a operator
     * @param ch [String]
     * @return [Boolean] either it is or not a operator
     */
    private fun isOperator(ch: String): Boolean {
        return ch == Constants.concat || ch == "|" || singleOperators.contains(ch)
    }


    /**
     * This function is to check if the val is a parenthesis
     * @param ch [String]
     * @return [Boolean] either it is or not a parenthesis
     */
    private fun isParenthesis(ch: String): Boolean {
        return ch == "(" || ch == ")"
    }


    /**
     * This function is the one in charge of getting the Tree from the posfix expression
     * @param postfix [List] The posftfix list of string
     * @return [RegexExpression] The tree
     */
    private fun expressionTreeList(postfix: List<String>): RegexExpression {
        val st: Stack<RegexExpression> = Stack<RegexExpression>()

        //We iterate
        for (i in postfix.indices) {
            //Get the lleter
            val letter = postfix[i]
            //If it is an operator we have to create the respective node
            if (isOperator(postfix[i])) {
                val nodeOp: RegexExpression
                when  {
                    //If it is a single operator we only pop one element from our stack and assign it
                    singleOperators.contains(letter) -> {
                        //Use the factory to create the single operator
                        nodeOp = SingleOperatorNodeFactory.create(letter)
                        if (st.empty()) {
                            throw Exception("El operador $letter esperaba una expresión a la cual se le pudiera asignar en el stack. 😬")
                        }
                        val a = st.pop()
                        nodeOp.setNode(a)
                    }
                    else -> {
                        //Use the factory to create the operator
                        nodeOp = OperatorNodeFactory.create(letter)
                        if (st.empty()) {
                            throw Exception("El operador $letter esperaba una expresión a la cual se le pudiera asignar en el lado izquierdo. 👈🏻")
                        }
                        //Get the right value
                        val right = st.pop()
//                        if (right is OrOperatorNode){
//                            throw Exception("El operador $letter no puede recibir ${right.expression} como valor del lado derecho. 👉🏻")
//                        }
                        if (st.empty()) {
                            throw Exception("El operador $letter esperaba una expresión a la cual se le pudiera asignar en el lado derecho. 👉🏻")
                        }
                        //Get the left value
                        val left = st.pop()
//                        if (left is OrOperatorNode){
//                            throw Exception("El operador $letter no puede recibir ${left.expression} como valor del lado izquierdo. 👈🏻")
//                        }
                        //We set the left and right child
                        nodeOp.setNode(left)
                        nodeOp.setNode(right)
                    }
                }

                st.push(nodeOp)
            } else {
                //It is just a letter
                st.push(WordNode(letter))
            }
        }
        return st.pop()
    }

    /**
     * We use this function to get a list of "tokens" so we can use CONCAT as a token
     * @param exp [List]
     * @return [List] List of postfix expression
     */
    private fun infixToPostfixList(exp: List<String>): List<String> {
        val result = mutableListOf<String>()
        val stack = Stack<String>()
        for (element in exp) {
            when {
                !isOperator(element) && !isParenthesis(element) -> result.add(element)
                element == "(" -> stack.push(element)
                element == ")" -> {
                    var resultCount = 0
                    //Stack is empty means the ( was not fund
                    if (stack.isEmpty()) {
                        throw java.lang.Exception("No hubo ( encontrado 😞")
                    }
                    while (!stack.isEmpty() &&
                        stack.peek() != "("
                    ) {
                        result.add(stack.pop())
                        resultCount++
                    }
                    //After keep searching we dont have a ( to pop it was not even started
                    if (stack.isEmpty()) {
                        throw java.lang.Exception("No hubo ( encontrado 😭")
                    }
                    //The parenthesis is empty
                    if (resultCount == 0){
                        throw Exception("El parentesis no puede estar vacio 🔎")
                    }
                    //Pop the (
                    stack.pop()
                }
                else -> {
                    //We get the precedence
                    val cPrec = precedence(element)
                    //While stack is not empty and the precedence of the element is lower than the head of the stack
                    //We will pop the value and add it to result
                    while (!stack.isEmpty() && cPrec <= precedence(stack.peek())
                    ) {
                        result.add(stack.pop())
                    }
                    stack.push(element)
                }
            }
        }


        //We finish closing the parenthesis
        while (!stack.isEmpty()) {
            if (stack.peek() == "(") throw Exception("No se cerro parentesis 😤")
            result.add(stack.pop())
        }
        return result
    }


    /**
     * Private function is is intended to normalize the string
     * and return it in a list to later process it as a postfix string
     * here we add [Constants.concat] between the chars
     */
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


    /**
     * Build function it creates our expression and builds the graph
     */
    fun build() : RegexExpression {
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

    /**
     * With this function we sorround the regex expression with a concat parent and a # right child
     */
    fun getDirectExpression() : DirectRegex {
        if (!this::expression.isInitialized){
            build()
        }
        return DirectRegex(this@DRegex.expression)

    }

    /**
     * Stores the graph in our outputs/tree directory
     */
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