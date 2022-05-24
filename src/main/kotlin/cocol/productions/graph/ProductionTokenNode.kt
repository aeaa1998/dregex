package cocol.productions.graph

import tokens.TokenExpression

class ProductionTokenNode(
    val token: String
    ) : ProductionNode() {
    var isAny = false
    override val isNullable: Boolean = false
    override fun generateBody(): String {
        return if (!isAny) """
            checkNextTokenIs("$token")
            ${semanticBody ?: ""}
        """.trimIndent() else """
            moveToken()
            ${semanticBody ?: ""}
        """.trimIndent()
    }

    override fun computePropertiesWrapped(productions: Map<String, ProductionFunction>) {
        hasAny = isAny
        firstPos.add(this)
        lastPos.add(this)
    }
}