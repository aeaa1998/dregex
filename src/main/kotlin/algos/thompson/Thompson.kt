package algos.thompson

import automatons.NFA
import dregex.*


class Thompson(
    val regex: String
) {

    private val visitor = ThompsonRegexVisitor(regex)
    lateinit var nfa: NFA
    fun build(){
        val dRegex = DRegex(regex)
        val expression = dRegex.build()
        expression.accept(visitor)
        val resultNFA = visitor.nfa
        //We build the graph
        visitor.buildGraph()
        nfa = resultNFA
    }

}