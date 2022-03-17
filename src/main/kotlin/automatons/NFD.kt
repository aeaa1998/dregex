package automatons

import extension.addAllUnique
import extension.addUnique
import extension.containsId
import java.util.*
import kotlin.collections.HashMap

class NFD<StateImpl: State>(
    override val states: MutableList<StateImpl>,
    override val initialState: StateImpl,
    override val transitions: HashMap<String, HashMap<String, StateImpl>>,
    override val finalStates: MutableList<StateImpl>
) : Automaton<StateImpl, StateImpl> {

    //Compute the alphabet when its needed we know its all the unique transition keys for each
    //state
    override val alphabet: List<String> by lazy {
        val alph = mutableListOf<String>()
        transitions.forEach { (_, u) -> alph.addAllUnique(u.keys) }
        alph
    }
    var currentState = initialState

    override fun move(currentState: StateImpl, letter: String): StateImpl? {
        val setOfTransitionsForState = transitions[currentState.secondaryId]
        setOfTransitionsForState?.let { possibleTransitions ->
            possibleTransitions[letter]?.let { newState ->
                return newState
            }

        }
        return null
    }

    override fun simulate(value:String) : Boolean {
        value.forEachIndexed { index, c ->
            val letter = c.toString()


            val nextForState = transitions[currentState.secondaryId]?.get(letter)


            //If it there is no next state value is not the last char
            if (nextForState == null && index != value.count() - 1 && value.count() != 1){
                return@simulate false
            }else if (nextForState != null){
                currentState = nextForState
            }

        }
        //final states contains current state
        return finalStates.containsId(currentState)
    }
}