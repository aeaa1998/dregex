package cocol.productions.graph

class ProductionGrouperNode(val grouper: String) : ProductionNode(){
    override val isNullable: Boolean = false
    override fun generateBody(): String {
        return """
            ${left?.generateBody() ?: ""}
            ${semanticBody ?: ""}
        """.trimIndent()
    }

    override fun computePropertiesWrapped(productions: Map<String, ProductionFunction>) {
        firstPos = left?.firstPos ?: mutableSetOf()
        lastPos = left?.lastPos ?: mutableSetOf()
    }
}

class ProductionParenthesis() : ProductionNode(){
    var startSemantic: String? = null
    override val isNullable: Boolean = left?.isNullable == true
    override fun generateBody(): String {
        return """
            ${startSemantic ?: ""}
            ${left?.generateBody() ?: ""}
            ${semanticBody ?: ""}
        """.trimIndent()
    }

    override fun computePropertiesWrapped(productions: Map<String, ProductionFunction>) {
        hasAny = left.isOrHasAny()
        firstPos = left?.firstPos ?: mutableSetOf()
        lastPos = left?.lastPos ?: mutableSetOf()
    }
}