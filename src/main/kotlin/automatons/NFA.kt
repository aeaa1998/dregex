package automatons

import extension.addAllUnique
import extension.addUnique
import extension.containsId
import utils.Constants

class NFA<StateImpl: State>(
    override val states: MutableList<StateImpl>,
    override val initialState: StateImpl,
    override val transitions: HashMap<String, HashMap<String, List<StateImpl>>>,
    override val finalStates: MutableList<StateImpl>
) : Automaton<StateImpl, List<StateImpl>> {

    var currentState = listOf(initialState)

    //Compute the alphabet when its needed we know its all the unique transition keys for each
    //state
    override val alphabet: List<String> by lazy {
        val alph = mutableListOf<String>()
        transitions.forEach { (_, u) -> alph.addAllUnique(u.keys) }
        alph
    }

    override fun move(currentState: List<StateImpl>, letter: String) : List<StateImpl> {
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

        return resultingStatesFromState
    }


    //This is the recursive call
    fun eClosure(states: List<StateImpl> = listOf()) : List<StateImpl> {
        val eClosureStatesList: MutableList<StateImpl> = mutableListOf<StateImpl>().apply {
            addAll(states)
        }



        val possibleStates = mutableListOf<StateImpl>()
        states.forEach { state ->
            val nestedPossible = (transitions[state.id]?.get(Constants.clean) ?: listOf())
            //Only the ones we dont have
            .filter{
                !states.contains(it)
            }
            possibleStates.addAllUnique(nestedPossible)
        }

        return if (possibleStates.isEmpty()) {
            //We will stop returning states when there are not
            eClosureStatesList
        }else{
            //Add all unique
            possibleStates.forEach { possibleState ->
                eClosureStatesList.addUnique(possibleState)
//                val nestedPossible = eClosure(possibleState, eClosureStatesList + states)
                val nestedPossible = eClosure(eClosureStatesList + states)
                eClosureStatesList.addAllUnique(nestedPossible)
            }
            eClosureStatesList.addAllUnique(possibleStates)

            eClosureStatesList
        }


    }

//    fun eClosure(states: List<StateImpl>) : List<StateImpl> {
//        val eClosureStatesList: MutableList<StateImpl> = mutableListOf()
//        eClosureStatesList.addAllUnique(states)
//        eClosureStatesList.addAllUnique(states.map(this::eClosure).flatten())
//
//        return eClosureStatesList
//    }

    override fun simulate(value:String) : Boolean {
        value.forEachIndexed { index, c ->
            val letter = c.toString()
            val newStates = mutableListOf<StateImpl>()
            eClosure(currentState).forEach { currentState ->
                val nextForState = eClosure(transitions[currentState.id]?.get(letter) ?: listOf())
                newStates.addAllUnique(nextForState)
            }
            //If it is empty and value is not the last char
            if (newStates.isEmpty() && index != value.count()){
                return false
            }
            currentState = newStates
        }
        //If it contains any
        return currentState.any { finalStates.containsId(it) }
    }
}