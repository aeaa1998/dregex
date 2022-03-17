package automatons

import java.util.*

/**
 * Purpose of this interface is to define the most important things a Automaton has so we dont forget
 */
interface Automaton<StateImpl: State, TransitionResult> {
    val states: MutableList<StateImpl>
    val initialState: StateImpl

    val transitions: HashMap<String, HashMap<String, TransitionResult>>

    val alphabet: List<String>
    val finalStates: MutableList<StateImpl>

    fun move(
        currentState: TransitionResult,
        letter: String
    ) : TransitionResult?

    fun simulate(value:String) : Boolean
}

