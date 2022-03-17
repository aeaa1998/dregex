package algos.thompson

import automatons.NFA
import automatons.State
import dregex.*


class Thompson(
    val regex: String
) {

    private val visitor = ThompsonRegexNfaBuilder(regex)
    lateinit var nfa: NFA<State>
    fun build() : Thompson {
        val dRegex = DRegex(regex)
        val expression = dRegex.build()
        expression.accept(visitor)
        val resultNFA = visitor.nfa
        //We build the graph
        visitor.buildGraph()
        nfa = resultNFA
        return this
    }

}