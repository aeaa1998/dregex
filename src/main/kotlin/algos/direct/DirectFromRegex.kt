package algos.direct

import algos.subgroupsConstruction.SetConstructorState
import algos.thompson.ThompsonRegexNfaBuilder
import automatons.NFA
import automatons.NFD
import automatons.State
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.layout.mxIGraphLayout
import com.mxgraph.model.mxICell
import com.mxgraph.util.mxCellRenderer
import dregex.BruteForceEndNode
import dregex.DRegex
import extension.allUnique
import extension.containsAnyId
import extension.containsId
import graphs.RegexEdge
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DefaultDirectedGraph
import java.awt.Color
import java.io.File
import javax.imageio.ImageIO

class DirectFromRegex(
    val regex: String
) {

    lateinit var nfd: NFD
    fun build(){
        val dRegex = DRegex(regex)
        val expression = dRegex.getDirectExpression()
        expression.setComputedProperties()
        val initialState = DirectFromRegexState(expression.firstPos)
        val statesStack = mutableListOf(initialState)
        val alphabet = regex.toCharArray().distinct().map { it.toString() }
        val finalStates = mutableListOf<DirectFromRegexState>()


        var pointerIndex = 0
        val newTransitions: HashMap<String, HashMap<String, DirectFromRegexState>> = hashMapOf()
        do {
            val pointer = statesStack[pointerIndex]
            pointer.marked = true
            for (letter in alphabet){
                val subU = pointer.values.filter { it.expression == letter }
                    .map {
                        it.followPos
                    }.flatten()

                if (subU.isNotEmpty()){

                    var U = DirectFromRegexState(subU)

                    //Does not contain U
                    if (statesStack.containsId(U).not()){
                        statesStack.add(U)
                        val isFinal = subU.any { it is BruteForceEndNode }
                        if (isFinal){
                            finalStates.add(U)
                        }
                    }else{
                        U = statesStack.first { it.id == U.id }
                    }
                    if (!newTransitions.containsKey(pointer.secondaryId)) newTransitions[pointer.secondaryId] = hashMapOf()
                    //Set transition
                    newTransitions[pointer.secondaryId]?.set(letter, U)
                }
            }
            //If there is another in out iterator proceed
            if (pointerIndex + 1 < statesStack.count()){
                pointerIndex++
            }else{
                break
            }
        }
        while (statesStack.any { it.marked.not() })

        nfd = NFD(
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

            directedGraph.addVertex((state as DirectFromRegexState).secondaryId)
        }
        nfd.transitions.forEach { state, targetsWithExp ->
            targetsWithExp.forEach { expression, target ->

                directedGraph.addEdge(
                    state,
                    (target as DirectFromRegexState).secondaryId,
                    RegexEdge(expression)
                )


            }

        }
        val imgFile = File("src/main/kotlin/outputs/direct/${regex}.png")
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