package automatons

import extension.addAllUnique
import extension.addUnique
import extension.containsId
import java.util.*
import kotlin.collections.HashMap

class NFD(
    override val states: MutableList<State>,
    override val initialState: State,
    override val transitions: HashMap<String, HashMap<String, State>>,
    override val finalStates: MutableList<State>
) : Automaton<State> {

    //Compute the alphabet when its needed we know its all the unique transition keys for each
    //state
    override val alphabet: List<String> by lazy {
        val alph = mutableListOf<String>()
        transitions.forEach { (_, u) -> alph.addAllUnique(u.keys) }
        alph
    }
    var currentState = initialState

    override fun move(currentState: State, letter: String): State? {
        val setOfTransitionsForState = transitions[currentState.id]
        setOfTransitionsForState?.let { possibleTransitions ->
            possibleTransitions[letter]?.let { newState ->
                return newState
            }

        }
        return null
    }

    override fun simulate(value:String) : Boolean {
        value.forEach { c ->
            val letter = c.toString()


            val nextForState = transitions[currentState.secondaryId]?.get(letter)


            //If it there is no next state value is not the last char
            if (nextForState == null && c != value.last() && value.count() != 1){
                return false
            }else if (nextForState != null){
                currentState = nextForState
            }

        }
        //final states contains current state
        return finalStates.containsId(currentState)
    }
}