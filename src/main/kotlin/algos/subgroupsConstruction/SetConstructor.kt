package algos.subgroupsConstruction

import algos.thompson.Thompson
import automatons.NFD
import automatons.State
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.layout.mxIGraphLayout
import com.mxgraph.model.mxICell
import com.mxgraph.util.mxCellRenderer
import de.vandermeer.asciitable.AsciiTable
import dregex.DirectRegex
import extension.containsAnyId
import extension.containsId
import graphs.RegexEdge
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DefaultDirectedGraph
import utils.Constants
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO
import javax.swing.SwingConstants


/**
 * Class to build the NFD from the thompson NFA
 * @constructor
 * @param regex [String] The string to build the Regex tree
 *
 * @property nfd [NFD] The nfd to initialize
 */
class SetConstructor(
    val regex: String
){
    lateinit var nfd : NFD<SetConstructorState>
    fun build() : SetConstructor {
        //We create our thompson object
        val thompson =Thompson(regex)
        //Build our nfa
        val nfa = thompson.build().nfa

        //Get the alphabet all the possible values excluding clean
        val alphabet = nfa.alphabet.filter { it != Constants.clean }
        //Our initial state of the nfa apply e closure to get all the current states
        val initialState = SetConstructorState(nfa.eClosure(listOf(nfa.initialState)))

        //Initialize our stack
        val statesStack = mutableListOf(initialState)
        //Initialize out list of final states
        val finalStates = mutableListOf<SetConstructorState>()

        //First case
        if (nfa.finalStates.containsAnyId(initialState.values)){
            finalStates.add(initialState)
        }

        var pointerIndex = 0
        val newTransitions: HashMap<String, HashMap<String, SetConstructorState>> = hashMapOf()
        //While there is atleast one in stack not marked
        do {
            val pointer = statesStack[pointerIndex]
            pointer.marked = true
            for (transitionLetter in alphabet){

                //We create U with the states by applying the transition letter and then e closure
                var U = SetConstructorState(nfa.eClosure(
                    nfa.move(pointer.values, transitionLetter)
                ))

                //Only if U is not empty
                if (U.values.isNotEmpty()){
                    //Does not contain U
                    if (statesStack.containsId(U).not()){
                        statesStack.add(U)
                        //Each new state
                        if (nfa.finalStates.containsAnyId(U.values)){
                            finalStates.add(U)
                        }
                    }else{
                        U = statesStack.first { it.id == U.id }
                    }
                    if (!newTransitions.containsKey(pointer.secondaryId)) newTransitions[pointer.secondaryId] = hashMapOf()
                    //Set transition
                    newTransitions[pointer.secondaryId]?.set(transitionLetter, U)
                }

            }
            //If there is another in out iterator proceed
            if (pointerIndex + 1 < statesStack.count()){
                pointerIndex++
            }else{
                break
            }
        }while (statesStack.any { it.marked.not() })

        val nfd = NFD(
            states = statesStack,
            initialState = initialState,
            transitions = newTransitions,
            finalStates = finalStates
        )
        this.nfd = nfd
        buildGraph(nfd)
        return this
    }
    fun <StateImpl: State>buildGraph(nfd: NFD<StateImpl>){
        val directedGraph = DefaultDirectedGraph<String, RegexEdge>(RegexEdge::class.java)
        nfd.states.forEach { state ->

            directedGraph.addVertex(state.secondaryId)
        }
        nfd.transitions.forEach { state, targetsWithExp ->
            targetsWithExp.forEach { expression, target ->

                directedGraph.addEdge(
                    state,
                    target.secondaryId,
                    RegexEdge(expression)
                )


            }

        }
        val imgFile = File("src/main/kotlin/outputs/nfd/${regex}.png")
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
        val at = AsciiTable()
        at.addRule()
        at.addRow("Nuevo estado", "Los que contiene", *nfd.alphabet.toTypedArray())
        at.addRule()
        nfd.states.forEach {
            val rowValues = mutableListOf<String>()
            rowValues.add(it.secondaryId)
            rowValues.add(it.id)
            nfd.alphabet.forEach { alph ->
                rowValues.add(nfd.transitions[it.secondaryId]?.get(alph)?.secondaryId ?: "")
            }
            at.addRow(*rowValues.toTypedArray())
            at.addRule()
        }
        println(at.render())
    }
}