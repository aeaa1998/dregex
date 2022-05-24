package cocol.productions.graph

class ProductionAnyTokenNode : ProductionNode(){
    override val isNullable: Boolean = false
    override fun generateBody(): String {
        return """
            ${semanticBody ?: ""}
        """.trimIndent()
    }

    override fun computePropertiesWrapped(productions: Map<String, ProductionFunction>) {

    }
}