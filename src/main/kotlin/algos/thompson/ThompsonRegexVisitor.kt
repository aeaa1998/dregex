package algos.thompson

import algos.RegexVisitor
import automatons.NFA
import automatons.State
import com.mxgraph.layout.hierarchical.mxHierarchicalLayout
import com.mxgraph.layout.mxIGraphLayout
import com.mxgraph.model.mxICell
import com.mxgraph.util.mxCellRenderer
import dregex.*
import graphs.RegexEdge
import org.jgrapht.ext.JGraphXAdapter
import org.jgrapht.graph.DefaultDirectedGraph
import java.awt.Color
import java.io.File
import java.util.HashMap
import javax.imageio.ImageIO

class ThompsonRegexVisitor(
    val regex: String
) : RegexVisitor {
    lateinit var nfa: NFA
    override fun visit(regex: WordNode) {
        val initialState = State()
        val finalState = State()
        val expression = regex.expression
        nfa = NFA(
            states = mutableListOf(initialState, finalState),
            finalStates = mutableListOf(finalState),
            initialState = initialState,
            transitions = hashMapOf(initialState.id to hashMapOf(expression to listOf(finalState)))
        )
    }

    override fun visit(regex: OneOrMoreOperatorNode) {
        val initialState = State()
        val finalState = State()
        val nestedThompsonVisitor = ThompsonRegexVisitor(regex.expression)
        val leftRegex = regex.left
        require(leftRegex != null)
        leftRegex.accept(nestedThompsonVisitor)
        val nestedNfa = nestedThompsonVisitor.nfa

        val initialHolder = nestedNfa.transitions[nestedNfa.initialState.id]
        nestedNfa.transitions.remove(nestedNfa.initialState.id)


        //instantiate the transitions
        val transitions = hashMapOf<String, HashMap<String, List<State>>>()
        transitions[initialState.id] = initialHolder?: hashMapOf()

        //Link the end transition of the nested to the end of the nfa
        val endNestedStatesTransitions = hashMapOf<String, List<State>>()
        endNestedStatesTransitions["Ɛ"] = listOf(finalState)
        nestedNfa.finalStates.forEach { nestedFinalState ->
            transitions[nestedFinalState.id] = endNestedStatesTransitions
        }

        //Link the nested nfa

        transitions.plusAssign(nestedNfa.transitions)



        val allStates = listOf(initialState, finalState) + nestedNfa.states.filter { it.id != nestedNfa.initialState.id }


        nfa = NFA(
            states = allStates.toMutableList(),
            finalStates = mutableListOf(finalState),
            initialState = initialState,
            transitions = transitions
        )
    }

    override fun visit(regex: ZeroOrMoreOperatorNode) {
        val initialState = State()
        val finalState = State()
        val nestedThompsonVisitor = ThompsonRegexVisitor(regex.expression)
        val leftRegex = regex.left
        require(leftRegex != null)
        leftRegex.accept(nestedThompsonVisitor)
        val nestedNfa = nestedThompsonVisitor.nfa
        val initialStatesTransitions = hashMapOf<String, List<State>>()
        initialStatesTransitions["Ɛ"] = listOf(finalState, nestedNfa.initialState)

        //instantiate the transitions
        val transitions = hashMapOf<String, HashMap<String, List<State>>>()
        transitions[initialState.id] = initialStatesTransitions

        //Link the end transition of the nested to the end of the nfa
        val endNestedStatesTransitions = hashMapOf<String, List<State>>()
        endNestedStatesTransitions["Ɛ"] = listOf(finalState, nestedNfa.initialState)
        nestedNfa.finalStates.forEach { nestedFinalState ->
            transitions[nestedFinalState.id] = endNestedStatesTransitions
        }

        //Link the nested nfa

        transitions.plusAssign(nestedNfa.transitions)



        val allStates = listOf(initialState, finalState) + nestedNfa.states


        nfa = NFA(
            states = allStates.toMutableList(),
            finalStates = mutableListOf(finalState),
            initialState = initialState,
            transitions = transitions
        )
    }

    override fun visit(regex: ZeroOrOne) {
        val initialState = State()
        val finalState = State()
        val nestedThompsonVisitor = ThompsonRegexVisitor(regex.expression)
        val leftRegex = regex.left
        require(leftRegex != null)
        leftRegex.accept(nestedThompsonVisitor)
        val nestedNfa = nestedThompsonVisitor.nfa
        val initialStatesTransitions = hashMapOf<String, List<State>>()
        initialStatesTransitions["Ɛ"] = listOf(finalState, nestedNfa.initialState)

        //instantiate the transitions
        val transitions = hashMapOf<String, HashMap<String, List<State>>>()
        transitions[initialState.id] = initialStatesTransitions

        //Link the end transition of the nested to the end of the nfa
        val endNestedStatesTransitions = hashMapOf<String, List<State>>()
        endNestedStatesTransitions["Ɛ"] = listOf(finalState)
        nestedNfa.finalStates.forEach { nestedFinalState ->
            transitions[nestedFinalState.id] = endNestedStatesTransitions
        }

        //Link the nested nfa

        transitions.plusAssign(nestedNfa.transitions)



        val allStates = listOf(initialState, finalState) + nestedNfa.states


        nfa = NFA(
            states = allStates.toMutableList(),
            finalStates = mutableListOf(finalState),
            initialState = initialState,
            transitions = transitions
        )
    }

    override fun visit(regex: OrOperatorNode) {
        val initialState = State()
        val finalState = State()
        val leftThompsonVisitor = ThompsonRegexVisitor(regex.expression)
        val rightThompsonVisitor = ThompsonRegexVisitor(regex.expression)
        val leftRegex = regex.left
        val rightRegex = regex.right
        require(leftRegex != null)
        require(rightRegex != null)
        leftRegex.accept(leftThompsonVisitor)
        rightRegex.accept(rightThompsonVisitor)
        val leftNfa = leftThompsonVisitor.nfa
        val rightNfa = rightThompsonVisitor.nfa

        val initialStatesTransitions = hashMapOf<String, List<State>>()
        initialStatesTransitions["Ɛ"] = listOf(leftNfa.initialState, rightNfa.initialState)

        //instantiate the transitions
        val transitions = hashMapOf<String, HashMap<String, List<State>>>()
        transitions[initialState.id] = initialStatesTransitions

        //Link the end transition of the nested to the end of the nfa
        leftNfa.finalStates.forEach { nestedFinalState ->
            transitions[nestedFinalState.id] = hashMapOf("Ɛ" to listOf(finalState))
        }
        rightNfa.finalStates.forEach { nestedFinalState ->
            transitions[nestedFinalState.id] = hashMapOf("Ɛ" to listOf(finalState))
        }


        //Link the nested nfa
        transitions.plusAssign(leftNfa.transitions)


        //Link the nested nfa
        transitions.plusAssign(rightNfa.transitions)


        val allStates = listOf(initialState, finalState) + leftNfa.states + rightNfa.states


        nfa = NFA(
            states = allStates.toMutableList(),
            finalStates = mutableListOf(finalState),
            initialState = initialState,
            transitions = transitions
        )
    }

    override fun visit(regex: WildCardOperatorNode) {

    }

    override fun visit(regex: ConcatNode) {
        val leftThompsonVisitor = ThompsonRegexVisitor(regex.expression)
        val rightThompsonVisitor = ThompsonRegexVisitor(regex.expression)
        val leftRegex = regex.left
        val rightRegex = regex.right
        require(leftRegex != null)
        require(rightRegex != null)
        leftRegex.accept(leftThompsonVisitor)
        rightRegex.accept(rightThompsonVisitor)
        val leftNfa = leftThompsonVisitor.nfa
        val rightNfa = rightThompsonVisitor.nfa
        val initialState = leftNfa.initialState
        val middleState = leftNfa.finalStates
        val transitions = leftNfa.transitions

        rightNfa.initialState.let { initialStateRight ->
            //We hold the value
            val initialTransitions = rightNfa.transitions[initialStateRight.id]
            require(initialTransitions != null)
            //We remove the value
            rightNfa.transitions.remove(initialStateRight.id)
            //Set the new values from the left state
            middleState.forEach { finalLeftState ->
                rightNfa.transitions[finalLeftState.id] = initialTransitions
            }

        }
        transitions.plusAssign(rightNfa.transitions)
        nfa = NFA(
            states = (leftNfa.states + rightNfa.states.filter { it.id != rightNfa.initialState.id }).toMutableList(),
            finalStates = rightNfa.finalStates,
            initialState = initialState,
            transitions = transitions
        )
    }

    fun buildGraph(){
        val directedGraph = DefaultDirectedGraph<String, RegexEdge>(RegexEdge::class.java)
        nfa.states.forEach { state -> directedGraph.addVertex(state.id) }
        nfa.transitions.forEach { state, targetsWithExp ->
            targetsWithExp.forEach { expression, targets ->
                targets.forEach { target ->
                    directedGraph.addEdge(
                        state,
                        target.id,
                        RegexEdge(expression)
                    )
                }
            }

        }

        val imgFile = File("src/main/kotlin/outputs/nfa/${regex}.png")
        if (imgFile.exists()){
            imgFile.delete()
            imgFile.createNewFile()
        }else{
            imgFile.createNewFile()
        }
        val graphAdapter = JGraphXAdapter(directedGraph)
        val vertexToCellMap: HashMap<String, mxICell> = graphAdapter.vertexToCellMap
        val arrayHolderFinal = mutableListOf<mxICell>()
        nfa.finalStates.forEach {
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