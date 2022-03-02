package automatons

import extension.addAllUnique
import extension.addUnique
import extension.containsId
import utils.Constants

class NFA(
    override val states: MutableList<State>,
    override val initialState: State,
    override val transitions: HashMap<String, HashMap<String, List<State>>>,
    override val finalStates: MutableList<State>
) : Automaton<List<State>> {

    var currentState = listOf(initialState)

    //Compute the alphabet when its needed we know its all the unique transition keys for each
    //state
    override val alphabet: List<String> by lazy {
        val alph = mutableListOf<String>()
        transitions.forEach { (_, u) -> alph.addAllUnique(u.keys) }
        alph
    }

    override fun move(currentState: List<State>, letter: String) : List<State> {
        val setOfTransitionsForState = currentState.map { state: State ->
            transitions[state.id] ?: hashMapOf()
        }.filter {
            //We need to filter the ones that are empty
            it.isNotEmpty()
        }


        val resultingStatesFromState = setOfTransitionsForState.map { transitions ->
            transitions[letter] ?: emptyList()
        }.filter { stateList ->
            //Only the resulting states
            stateList.isNotEmpty()
        }.flatten()
//        currentState = resultingStatesFromState
        return resultingStatesFromState
    }


    fun eClosure(state: State, states: List<State> = listOf()) : List<State> {
        val eClosureStatesList: MutableList<State> = mutableListOf(state)

        val possibleStates = (transitions[state.id]?.get(Constants.clean) ?: listOf())
                //Only the ones we dont have
            .filter{
                !states.contains(it)
            }

        return if (possibleStates.isEmpty()) {
            //We will stop returning states when there are not
            eClosureStatesList
        }else{
            //Add all unique
            possibleStates.forEach { possibleState ->
                eClosureStatesList.addUnique(possibleState)
                val nestedPossible = eClosure(possibleState, eClosureStatesList + states)
                eClosureStatesList.addAllUnique(nestedPossible)
            }
            eClosureStatesList.addAllUnique(possibleStates)

            eClosureStatesList
        }


    }

    fun eClosure(states: List<State>) : List<State> {
        val eClosureStatesList: MutableList<State> = mutableListOf()
        eClosureStatesList.addAllUnique(states)
        eClosureStatesList.addAllUnique(states.map(this::eClosure).flatten())

        return eClosureStatesList
    }

    override fun simulate(value:String) : Boolean {
        value.forEach { c ->
            val letter = c.toString()
            val newStates = mutableListOf<State>()
            currentState.forEach { currentState ->
                val nextForState = eClosure(transitions[currentState.id]?.get(letter) ?: listOf())

                newStates.addAllUnique(nextForState)

            }
            //If it is empty and value is not the last char
            if (newStates.isEmpty() && c != value.last()){
                return false
            }
            currentState = newStates
        }
        //If it contains any
        return currentState.any { finalStates.containsId(it) }
    }
}