package algos.direct

import automatons.NFD
import automatons.State
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.layout.mxIGraphLayout
import com.mxgraph.model.mxICell
import com.mxgraph.util.mxCellRenderer
import de.vandermeer.asciitable.AsciiTable
import dregex.BruteForceEndNode
import dregex.DirectRegex
import dregex.RegexExpression
import extension.containsId
import graphs.RegexEdge
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DefaultDirectedGraph
import tokens.TokenExpression
import utils.Constants
import java.awt.Color
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import javax.swing.SwingConstants

/**
 * Class to build the NFD directly from the regex.
 * @constructor
 * @param regex [String] The string to build the Regex tree
 *
 * @property nfd [NFD] The nfd to initialize
 * @property regexExpression [DirectRegex] The direct regex expression tree
 */
class DirectFromRegex(
    val regexString: String,
    val regex: RegexExpression
) {

    lateinit var nfd: NFD<DirectRegexSimplified>
    lateinit var regexExpression: DirectRegex

    /**
     * Function to build the NFD and plot it
     * @return [DirectFromRegex] We return the instance to let us an easier access to the nfd
     */
    fun build() : DirectFromRegex {
        //Build the tree

        //With #
        val expression = DirectRegex(regex)
        this.regexExpression = expression

        expression.setComputedProperties()
//        val stack = Stack<RegexExpression>()
//        stack.push(expression)
//        var head: RegexExpression = expression

//        while (!stack.isEmpty()){
//            val current = stack.peek()
//            val finishedSubtrees = current.right === head || current.left === head
//            val isLeaf = current.left == null && current.right == null
//            if (finishedSubtrees || isLeaf){
//                stack.pop()
//                //Set all the properties like followPos, startPos, endPos
//                current.setComputedProperties()
//                head = current
//            }else{
//                if (current.right != null){
//                    stack.push(current.right)
//                }
//                if (current.left != null){
//                    stack.push(current.left)
//                }
//            }
//
//
//        }

        //Set an initial state
        val initialState = DirectFromRegexState(expression.firstPos.toList())
        //Add it to our stack
        val statesStack = mutableListOf(initialState)
        //Get the alphabet
        val alphabet = regexString.toCharArray().distinct().map { it.toString() }
        //Init our final states list
        val finalStates = mutableListOf<DirectFromRegexState>()

        //Pointer to tell where we are
        var pointerIndex = 0
        //Al the transitions we are storing
        val newTransitions: HashMap<String, HashMap<String, DirectRegexSimplified>> = hashMapOf()
        val transitionTokens: HashMap<String, HashMap<String, TokenExpression>> = hashMapOf()
        do {
            //Get the current pointer
            val pointer = statesStack[pointerIndex]
            //Mark it as true
            pointer.marked = true
            //Iterate the alphabet
            for (letter in alphabet){
                //Ok so we will get all the regex expressions nodes where their expression is equal to the current letter
                val validValuesFromPointer = pointer.values.filter { it.expression == letter }
                val subU = validValuesFromPointer
                    //We get all the of the following positions
                    .map {
                        it.followPos
                    }
                    //We need to flat the array
                    .flatten()
                    //Get unique values by id
                    .distinctBy { it.id }

                //If the new group is not empty we must proceed
                if (subU.isNotEmpty()){
                    //Get the U node
                    var U = DirectFromRegexState(subU)
                    val isFinal = subU.any { it is BruteForceEndNode }
                    //Does not contain U
                    if (statesStack.containsId(U).not()){
                        //We add it
                        statesStack.add(U)
                        //Check if any of its expressions is the last one (terminal state)

                        if (isFinal){
                            //Add it
                            finalStates.add(U)
                        }
                    }else{
                        //If it contains the value from the stack we get its reference
                        U = statesStack.first { it.id == U.id }
                    }

                    if (isFinal){
                        val tks = validValuesFromPointer
                            .filter { !(it is BruteForceEndNode) && it.followPos.any { follow -> follow is BruteForceEndNode } }
//                            .distinctBy {
//                                it.tokenExpression.ident
//                            }
                            .distinctBy {
                                it.tokenExpression.ident
                            }
                            .sortedByDescending {
                                it.tokenExpression.weight
                            }
                            .map { it.tokenExpression }
                            .iterator()


                        var tk = tks.next()
//                        var check = tk
//                        while (tks.hasNext() && check.exceptKeywords){
//                            val compare = tks.next()
//                            if (compare.type.isKeyWord() && check.exceptKeywords && compare._expression.last().toString() == letter){
//                                tk = compare
//                            }
//                            check = compare
//                        }

                        if (tks.hasNext() && tk.exceptKeywords){
                            val compare = tks.next()
                            if (compare.type.isKeyWord() && tk.exceptKeywords && compare._expression.last().toString() == letter){
                                tk = compare
                            }
                        }

                        val current = transitionTokens[pointer.secondaryId]?.get(letter)
                        if (current == null){
                            if (!transitionTokens.containsKey(pointer.secondaryId)) transitionTokens[pointer.secondaryId] = hashMapOf()
                            transitionTokens[pointer.secondaryId]?.set(letter, tk)
                        }else if (current.weight < tk.weight && (current.type.isKeyWord() && tk.exceptKeywords && current._expression.last().toString() == letter).not()){
                            transitionTokens[pointer.secondaryId]?.set(letter, tk)
                        }
                    }


                    //If there are no transitions group created we create them
                    if (!newTransitions.containsKey(pointer.secondaryId)) newTransitions[pointer.secondaryId] = hashMapOf()
                    //Set transition
                    newTransitions[pointer.secondaryId]?.set(letter, U.simplified())

                }
            }
            //If there is another in out iterator proceed
            if (pointerIndex + 1 < statesStack.count()){
                pointerIndex++
            }else{
                break
            }
        }
            //While there is at least one state market we will continue
        while (statesStack.any { it.marked.not() })
        val statesSimplified = statesStack.map {
            it.simplified()
        }.toMutableList()
        //Set our NFD
        nfd = NFD(
            states = statesSimplified,
            initialState = initialState.simplified(),
            transitions = newTransitions,
            finalStates = finalStates.map { it.simplified() }.toMutableList(),
            transitionTokens = transitionTokens
        )

//        buildGraph(nfd)
        return this
    }


    fun <StateImpl: State> buildGraph(nfd: NFD<StateImpl>){
        val directedGraph = DefaultDirectedGraph<String, RegexEdge>(RegexEdge::class.java)
        nfd.states.forEach { state ->
            directedGraph.addVertex(state.secondaryId)
        }

        nfd.transitions.forEach { state, targetsWithExp ->
            val grouped: HashMap<String, MutableList<String>> = hashMapOf()
            targetsWithExp.forEach { expression, target ->
                val sec = target.secondaryId
                if (grouped.containsKey(sec).not()) grouped[sec] = mutableListOf()
                grouped[sec]?.add(expression)
            }
            grouped.forEach { target, expressions ->
                val expression = expressions.joinToString(", ")
                directedGraph.addEdge(
                    state,
                    target,
                    RegexEdge(expression)
                )
            }

        }
        val imgFile = File("src/main/kotlin/outputs/direct/${regexString
            .replace("/", "")
            .substring(0, regexString.count() % 10)}.png")
        if (imgFile.exists()){
            imgFile.delete()
            imgFile.createNewFile()
        }else{
            imgFile.createNewFile()
        }
        val graphAdapter = JGraphXAdapter(directedGraph)


        val vertexToCellMap: HashMap<String, mxICell> = graphAdapter.vertexToCellMap
        val arrayHolderFinal = mutableListOf<mxICell>()
        nfd.finalStates.forEach {
            vertexToCellMap[it.secondaryId]?.let { it1 -> arrayHolderFinal.add(it1) }
        }
        if (arrayHolderFinal.isNotEmpty()){
            graphAdapter.setCellStyle(
                "strokeColor=#CCCC00",
                arrayHolderFinal.toTypedArray()
            )
        }

        arrayHolderFinal.clear()

        nfd.initialState.let {
            vertexToCellMap[it.secondaryId]?.let { it1 -> arrayHolderFinal.add(it1) }
        }
        if (arrayHolderFinal.isNotEmpty()){
            graphAdapter.setCellStyle(
                "strokeColor=#0000FF",
                arrayHolderFinal.toTypedArray()
            )
        }


        val layout: mxIGraphLayout = mxHierarchicalLayout(graphAdapter, SwingConstants.WEST)
        layout.execute(graphAdapter.defaultParent)

        val image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2.0, Color.WHITE, true, null)

        ImageIO.write(image, "PNG", imgFile)
    }

    fun printDescription() {
        println("\nDESCRIPCION METODO DIRECTO\n")
        val at = AsciiTable()
        at.addRule()
        at.addRow("Nuevo estado", "Expresiones que contiene", *nfd.alphabet.toTypedArray())
        at.addRule()
        nfd.states.forEach {
            val rowValues = mutableListOf<String>()
            rowValues.add(it.secondaryId)
//            rowValues.add(it.values.map { it.expression }.joinToString(", "))
            nfd.alphabet.forEach { alph ->
                rowValues.add(nfd.transitions[it.secondaryId]?.get(alph)?.secondaryId ?: "")
            }
            at.addRow(*rowValues.toTypedArray())
            at.addRule()
        }
        println(at.render())
    }

}