package automatons

import java.util.*


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

