package algos.subgroupsConstruction

import algos.thompson.Thompson
import automatons.NFD
import automatons.State
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.layout.mxIGraphLayout
import com.mxgraph.model.mxICell
import com.mxgraph.util.mxCellRenderer
import extension.containsAnyId
import extension.containsId
import graphs.RegexEdge
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DefaultDirectedGraph
import utils.Constants
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO


class SetConstructor(
    val regex: String
){
    fun build(){
        val thompson =Thompson(regex)
        thompson.build()
        val nfa = thompson.nfa
        val alphabet = nfa.alphabet.filter { it != Constants.clean }
        val initialState = SetConstructorState(nfa.eClosure(nfa.initialState))

        val statesStack = mutableListOf(initialState)
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
            states = statesStack as MutableList<State>,
            initialState = initialState,
            transitions = newTransitions as HashMap<String, HashMap<String, State>>,
            finalStates = finalStates as MutableList<State>
        )
        buildGraph(nfd)
    }
    fun buildGraph(nfd: NFD){
        val directedGraph = DefaultDirectedGraph<String, RegexEdge>(RegexEdge::class.java)
        nfd.states.forEach { state ->

            directedGraph.addVertex((state as SetConstructorState).secondaryId)
        }
        nfd.transitions.forEach { state, targetsWithExp ->
            targetsWithExp.forEach { expression, target ->

                directedGraph.addEdge(
                    state,
                    (target as SetConstructorState).secondaryId,
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


        val layout: mxIGraphLayout = mxHierarchicalLayout(graphAdapter)
        layout.execute(graphAdapter.defaultParent)

        val image = mxCellRenderer.createBufferedImage(graphAdapter, null, 2.0, Color.WHITE, true, null)

        ImageIO.write(image, "PNG", imgFile)
    }
}