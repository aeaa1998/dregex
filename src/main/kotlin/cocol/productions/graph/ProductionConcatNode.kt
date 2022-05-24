package cocol.productions.graph

class ProductionConcatNode : ProductionNode() {
    override val isNullable: Boolean = false
    override fun generateBody(): String  = """
        ${left?.generateBody() ?: ""}
        ${right?.generateBody() ?: ""}
        ${semanticBody ?: ""}
    """.trimIndent()

    override fun computePropertiesWrapped(productions: Map<String, ProductionFunction>) {
        hasAny = left.isOrHasAny() || right.isOrHasAny()
        left?.firstPos?.let {
            firstPos.addAll(it)
        }

        right?.lastPos?.let { lastPos.addAll(it) }

        if (left?.isNullable == true){
            right?.firstPos?.let {
                firstPos.addAll(it)
            }
        }

        if (right?.isNullable == true){
            left?.lastPos?.let {
                lastPos.addAll(it)
            }
        }

        left?.lastPos?.forEach { lastPos ->
            lastPos.followPos.addAll(right?.firstPos ?: mutableSetOf())
        }


    }
}