package cocol.productions.graph


abstract class ProductionNode {
    abstract val isNullable: Boolean
    var hasAny: Boolean = false
    var semanticBody: String? = null
    var parent: ProductionNode? = null
    var left: ProductionNode? = null
    set(value) {
        field = value
        field?.parent = this
    }
    var right: ProductionNode? = null
        set(value) {
            field = value
            field?.parent = this
        }
    var positionsInited: Boolean = false
    var called: Boolean = false
    //Al the ways it can start
    var firstPos: MutableSet<ProductionTokenNode> = mutableSetOf()
    var lastPos: MutableSet<ProductionTokenNode> = mutableSetOf()
    var followPos: MutableSet<ProductionTokenNode> = mutableSetOf()
    abstract fun generateBody(): String
    protected abstract fun computePropertiesWrapped(productions: Map<String, ProductionFunction>)
    fun computeProperties(productions: Map<String, ProductionFunction>){
        called = true
        if (left?.positionsInited == false){
            left?.computeProperties(productions)
        }
        if (right?.positionsInited == false) {
            right?.computeProperties(productions)
        }

        if (positionsInited.not()){
            computePropertiesWrapped(productions)
        }
        positionsInited = true

    }


}

fun ProductionNode?.isOrHasAny(): Boolean {
    return this?.hasAny == true
}

