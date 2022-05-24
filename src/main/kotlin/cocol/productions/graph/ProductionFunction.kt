package cocol.productions.graph

class ProductionFunction(
    val name: String,
    val params: String,
    val semanticRule: String?
)  {
    var root = false
    var productionTree: ProductionNode? = null
    val firstPos: MutableSet<ProductionTokenNode>
    get() = productionTree?.firstPos ?: mutableSetOf()

    val lastPos: MutableSet<ProductionTokenNode>
        get() = productionTree?.lastPos ?: mutableSetOf()

    val followPos: MutableSet<ProductionTokenNode>
        get() = productionTree?.followPos ?: mutableSetOf()

    fun computeProperties(productions: Map<String, ProductionFunction>){
        productionTree?.computeProperties(productions)
    }


    fun generateBody(): String {
        return """
            fun $name($params){
                ${semanticRule ?: ""}
                
                ${productionTree?.generateBody() ?: ""}
            }
        """.trimIndent()
    }

}