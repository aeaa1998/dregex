package cocol.productions.graph;

import cocol.CocolScanner

class ProductionOneOrZeroNode : ProductionNode()  {
    override val isNullable: Boolean = true
    var startSemantic: String? = null
    override fun generateBody(): String {
//        val nextPosListValues = (left?.followPos ?: mutableSetOf()).filter { !it.isAny }.map { "\"${it.token}\"" }.joinToString(", ")
//        val nextPosList = "listOf<String>(${nextPosListValues})"
        //FIRST POS
        val firstPosListValues = firstPos.filter { !it.isAny }.map { "\"${it.token}\"" } .joinToString(", ")
        val firstPosList = "listOf<String>(${firstPosListValues}).contains(nextTokenMatch.token.ident)"
        var condition: String = firstPosList
//        (left as? ProductionTokenNode)?.let { token ->
        if (firstPos.filter { it.isAny }.isNotEmpty()){
            //Get all of the last pos that can continue this sequence
            val filter = lastPos.flatMap { it.followPos }.map { it.token }
            val idents = CocolScanner.tokens.map { it.ident }
                .filter { !filter.contains(it) }
                .map { "\"${it}\"" }
                .joinToString(", ")
            condition += " || listOf<String>(${idents}).contains(nextTokenMatch.token.ident)"
        }
//        }

        return """
            if($condition){
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
