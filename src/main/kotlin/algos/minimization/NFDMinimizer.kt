package algos.minimization

import automatons.NFD
import automatons.State

class NFDMinimizer(
    val nfd: NFD
) {
    fun min(){
        val final = nfd.finalStates
        val ids = final.map { it.secondaryId }
        val rest = nfd.states.filter { !ids.contains(it.secondaryId) }
        val restIds = rest.map { it.secondaryId }
        val restTransitions = nfd.transitions.filter { restIds.contains(it.key) }
        val finalTransitions = nfd.transitions.filter { ids.contains(it.key) }
//        val equivalents = mutableListOf<Equivalents>(Equivalents(rest), Equivalents(final))
//        var groups = listOf(rest, final)
        var finalGroups = listOf<List<State>>()


//        val combinations = mutableListOf<Pair<State, State>>()
//        rest.forEach { state ->
//        rest.filter { it.secondaryId != state.secondaryId }.forEach { pair ->
//                combinations.add(Pair(state, pair))
//            }
//        }



    }
}

class Equivalents(
    val states: MutableList<State>,
){
    var isTheSame: Boolean = false
    val id: String
    get() = states.map { it.secondaryId }.sorted().joinToString("-")


    fun containsState(state: State) : Boolean = states.map { it.secondaryId }.contains(state.secondaryId)


    fun divide(
        transitions: HashMap<String, HashMap<String, State>>,
        otherEquivalents: List<Equivalents>,
        alphabet: List<String>
    ){
        val equivalentsHolder: List<Equivalents> = listOf(this)
        val combinations = mutableListOf<Pair<State, State>>()
        states.forEach { state ->
            states.filter { it.secondaryId != state.secondaryId }.forEach { pair ->
                combinations.add(Pair(state, pair))
            }
        }
        combinations.forEach { combination ->
            val first = combination.first
            val firstTransitions = transitions[first.secondaryId] ?: hashMapOf()
            val second = combination.second
            val secondTransitions = transitions[second.secondaryId] ?: hashMapOf()
            alphabet.all { alph ->
                val transitionOne = firstTransitions[alph]
                val transitionTwo = firstTransitions[alph]
                if (transitionOne != null && transitionTwo != null){

                    val sharedEquivalent = otherEquivalents.firstOrNull { it.containsState(transitionOne) && it.containsState(transitionTwo) }
//                        ?: equivalentsHolder.firstOrNull { it.containsState(transitionOne) && it.containsState(transitionTwo) }
                    return@all sharedEquivalent != null
                }else if (transitionOne != null) {

                }
                else{
                    //Because both doesnt have a transition
                    return@all true
                }

                (transitionOne?.secondaryId ?: "EMPTY") == (transitionTwo?.secondaryId ?: "EMPTY")
            }
        }
    }
}