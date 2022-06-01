package cocol.productions.graph

import cocol.CocolScanner
import cocol.productions.graph.ProductionNode


class ProductionZeroOrModeNode() : ProductionNode() {
    override val isNullable: Boolean = true
    var startSemantic: String? = null
    override fun generateBody(): String {
        //FIRST POS
        val firstPosListValues = firstPos.filter { !it.isAny }.map { "\"${it.token}\"" } .joinToString(", ")
        val conditionList: MutableList<String> = mutableListOf()

        if (firstPosListValues.isNotEmpty()){
            val firstPosList = "listOf<String>(${firstPosListValues}).contains(nextTokenMatch.token.ident)"
            conditionList.add(firstPosList)
        }



//        (left as? ProductionTokenNode)?.let { token ->
            if (firstPos.filter { it.isAny }.isNotEmpty()){
                //Get all of the last pos that can continue this sequence
                val filter = lastPos.flatMap { it.followPos }.map { it.token }
                val idents = CocolScanner.tokens.map { it.ident }
                    .filter { !filter.contains(it) }
                    .map { "\"${it}\"" }
                    .joinToString(", ")
                conditionList.add("listOf<String>(${idents}).contains(nextTokenMatch.token.ident)")
            }
//        }
        val condition: String = conditionList.joinToString(" || ")
        return """
            
        while($condition){
                ${startSemantic ?: ""}
                ${left?.generateBody() ?: ""}
                
            }
            ${semanticBody ?: ""}
        """.trimIndent()
    }

    override fun computePropertiesWrapped(productions: Map<String, ProductionFunction>) {
        hasAny = left.isOrHasAny()
        left?.firstPos?.let {
            firstPos.addAll(it)
        }
        left?.lastPos?.let { lastPos.addAll(it) }
        lastPos = left?.lastPos ?: mutableSetOf()
        //
        lastPos.forEach { lastPos ->
            lastPos.followPos.addAll(firstPos)
        }
    }

}