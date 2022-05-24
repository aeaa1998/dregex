package cocol.productions.graph

class ProductionOrNode : ProductionNode() {
    override val isNullable: Boolean = false
    fun generateConditionBody(node: ProductionNode?): String?{
        return if ((node as? ProductionTokenNode)?.isAny == true) {
            """
               else
            """.trimIndent()
        }else{
            (node?.firstPos ?: emptyList()).map {
                """
                nextTokenMatch.token.ident == "${it.token}"
            """.trimIndent()
            }.joinToString(" || ")
        }

    }

    private fun isAny(node: ProductionNode?): Boolean {
        return (node as? ProductionTokenNode)?.isAny == true
    }

    override fun generateBody(): String {
        val leftCondition = generateConditionBody(left)

        val rightCondition = generateConditionBody(right)
//        val hasAny = left.isOrHasAny() || right.isOrHasAny()
        val isAny = isAny(left) || isAny(right)
        return """
            when {
                $leftCondition -> {
                    ${left?.generateBody() ?: ""}
                }
                
                $rightCondition -> {
                    ${right?.generateBody() ?: ""}
                }
                ${
                    //It can init with an any
                    if (firstPos.filter { it.isAny }.isNotEmpty())
                        if(!isAny) "else -> {  moveToken() }"
                        else "" 
                    else """
                    else -> {
                        println("No se pudo resolver ni una de las condiciónes para el valor ${ '$' }{nextTokenMatch.match}.")
                    }
                    """.trimIndent()
                }
                
            }
            
            ${semanticBody ?: ""}
        """.trimIndent()
    }

    override fun computePropertiesWrapped(productions: Map<String, ProductionFunction>) {
        hasAny = left.isOrHasAny()
        firstPos = ((left?.firstPos ?: mutableSetOf()) + (right?.firstPos ?: mutableSetOf())).toMutableSet()
        lastPos = ((left?.lastPos ?: mutableSetOf()) + (right?.lastPos ?: mutableSetOf())).toMutableSet()
        left?.lastPos?.forEach { lastPos ->
            lastPos.followPos.addAll(right?.firstPos ?: mutableSetOf())
        }
    }
}