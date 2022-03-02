package automatons

import java.util.*
import kotlin.collections.HashMap

interface Automaton<TransitionResult> {
    val states: MutableList<State>
    val initialState: State

    val transitions: HashMap<String, HashMap<String, TransitionResult>>

    val alphabet: List<String>
    val finalStates: MutableList<State>

    fun move(
        currentState: TransitionResult,
        letter: String
    ) : TransitionResult?

    fun simulate(value:String) : Boolean

}

