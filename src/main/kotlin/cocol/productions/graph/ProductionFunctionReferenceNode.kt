package cocol.productions.graph

class ProductionFunctionReferenceNode(
    val functionName: String,
    val functionParams: String
) : ProductionNode() {
    override val isNullable: Boolean = false

    override fun generateBody(): String {
        return  """
            $functionName($functionParams)
            ${semanticBody ?: ""}
        """.trimIndent()
    }

    override fun computePropertiesWrapped(productions: Map<String, ProductionFunction>) {
        val productionRef = productions[functionName]
        requireNotNull(productionRef){
            "La función $functionName no fue inicializada"
        }
        if (productionRef.productionTree?.called == false){
            productionRef.computeProperties(productions)
        }
        hasAny = productionRef.productionTree?.hasAny == true
        firstPos = productionRef.firstPos
        lastPos = productionRef.lastPos
        followPos = productionRef.followPos

    }
}