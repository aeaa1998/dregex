package graphs

import org.jgrapht.graph.DefaultEdge

class RegexEdge(
    val expression: String
) : DefaultEdge() {
    override fun toString(): String {
        return expression
    }
}