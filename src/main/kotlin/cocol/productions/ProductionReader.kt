package cocol.productions

import cocol.CocolScanner
import cocol.StringNormalizer
import cocol.anyCharSet
import cocol.characters.CocolTextValueGetter
import cocol.productions.graph.*
import dregex.*
import extension.getNextTokenIsNotWhiteSpace
import tokens.TokenExpression
import tokens.TokenMatch
import utils.CocolLangIdents
import utils.Postfixable
import java.lang.StringBuilder
import java.util.*

class ProductionReader(
    val functionTokenName: TokenMatch,
    val tokensSearchIterator: ListIterator<TokenMatch>,
    val tokens: List<TokenExpression>
) : Postfixable<ProductionNode, ProductionNode> {
    val tokensIdents: List<String> = tokens.map { it.ident }
    lateinit var productionNode: ProductionNode
    fun readProduction(): ProductionFunction {

        var paramBody: String = ""
        //Check it is an ident
        if (functionTokenName.token.ident != CocolLangIdents.Ident.ident){
            throw Exception("Al encontrar la producción se esperaba un ident como nombre de la producción se recibio: " + functionTokenName.token.ident)
        }
        //Set the function name
        val functionName: String = functionTokenName.match

        var _tt = tokensSearchIterator.next()
        //Ignore idents
        while (_tt.token.ident == CocolLangIdents.WhiteSpace.ident && tokensSearchIterator.hasNext()){
            _tt = tokensSearchIterator.next()
        }
        var nextToken = _tt
        //It has params
        getActionBlock(nextToken)?.let { paramBodyFound ->
            paramBody = paramBodyFound
            nextToken = getNextTokenIsNotWhiteSpace(tokensSearchIterator)
        }

        if (nextToken.token.ident != CocolLangIdents.Assign.ident){
            throw Exception("Se esperaba una asignación en productions")
        }
        _tt = tokensSearchIterator.next()
        while (_tt.token.ident == CocolLangIdents.WhiteSpace.ident && tokensSearchIterator.hasNext()){
            _tt = tokensSearchIterator.next()
        }
        nextToken = _tt

        var semanticRule: String? = null
        if (nextToken.token.ident == CocolLangIdents.SemanticRule.ident){
            semanticRule = getSemanticRule()
        }else{
            tokensSearchIterator.previous()
        }

        val tree =  tree()
        return ProductionFunction(
            name = functionName,
            paramBody,
            semanticRule
        ).apply {
            this.productionTree = tree
        }

    }

    private fun getSemanticRule() : String {
        var token: TokenMatch = tokensSearchIterator.next()
        var semanticRule = StringBuilder()

        while (tokensSearchIterator.hasNext()){
            if (token.token.ident == CocolLangIdents.SemanticRule.ident && token.match != ".)"){
                throw Exception("Intentado crear regla semantica dentro de la misma.")
            }else if (token.token.ident == CocolLangIdents.SemanticRule.ident && token.match == ".)"){
                break
            }
            //Add match
            semanticRule = semanticRule.append(token.match)
            //Move to next token
            token = tokensSearchIterator.next()

        }

        return semanticRule.toString()
    }

    private fun tree(): ProductionNode {
        var finalized = false
        val productionNodes = mutableListOf<ProductionNode>()
        //While it is not the final operator and can continue
        while (tokensSearchIterator.hasNext()){

            val token = tokensSearchIterator.next()
            if (CocolLangIdents.WhiteSpace.ident != token.token.ident){
                val node: ProductionNode

                //Check on the ident
                when(token.token.ident){
                    //We found a
                    CocolLangIdents.Ident.ident -> {
                        //If the ident is contained in the tokens list
                        if (tokensIdents.contains(token.match)){
                            node = ProductionTokenNode(token.match)
                        }else{
                            //In case is another production
                            val params = getNextTokenIsNotWhiteSpace(tokensSearchIterator)
                            var paramsBody: String? = null
                            if (params.token.ident == CocolLangIdents.ActionParamBlockStart.ident){
                                //Set the param body
                                paramsBody = getActionBlock(params)
                            }else{
                                //Move cursor back
                                tokensSearchIterator.previous()
                            }
                            node = ProductionFunctionReferenceNode(
                                token.match,
                                paramsBody ?: ""
                            )
                        }
                    }

                    //We found an operator
                    CocolLangIdents.Operator.ident -> {
                        val validOperators = listOf("|", ".")
                        if (validOperators.contains(token.match).not()){
                            throw Exception("El operador: ${token.match} no es válido en las producciones.")
                        }

                        if (token.match == "."){
                            finalized = true
                            break
                        }else{
                            //It is or operator
                            node = ProductionOrNode()
                        }
                    }

                    //We found a new raw token
                    CocolLangIdents.StringIdent.ident -> {
                        val charNormalized = StringNormalizer(CocolTextValueGetter(token.match).get()).normalize()
                        val identRaw = "raw $charNormalized"
                        node = ProductionTokenNode(identRaw)
                        CocolScanner.addRawToken(identRaw, "\\$charNormalized")
                    }

                    CocolLangIdents.Grouper.ident -> {
                        node = ProductionGrouperNode(token.match)
                    }

                    CocolLangIdents.WhiteSpace.ident -> {
                        continue
                    }

                    CocolLangIdents.Any.ident -> {
                        val identRaw = "raw Any"
                        node = ProductionTokenNode(identRaw).apply {
                            isAny = true
                        }
                        CocolScanner.addAnyToken()
                    }

                    else -> {
                        throw Exception("Token no válido para las producciones.")
                    }


                }

                var _tt = tokensSearchIterator.next()
                while (_tt.token.ident == CocolLangIdents.WhiteSpace.ident && tokensSearchIterator.hasNext()){
                    _tt = tokensSearchIterator.next()
                }
                val nextToken = _tt

                //It has a semantic rule
                if (nextToken.token.ident == CocolLangIdents.SemanticRule.ident){
                    val semanticRule = getSemanticRule()
                    //Set the semantic rule
                    node.semanticBody = semanticRule
                }else{
                    //It does not have a semantic rule go back
                    tokensSearchIterator.previous()
                }
                //Add the node
                productionNodes.add(node)
            }
        }

        if (!finalized){
            throw Exception("No se ha finalizado la producción de manera correcta")
        }

        val normalizedProductionNodes = mutableListOf<ProductionNode>()
        val validFinishArray = listOf(')', '}', ']').map { it.toString() }

        productionNodes.forEachIndexed { index, node ->
            val grouperStart = (node as? ProductionGrouperNode)?.grouper
            val validStart = (grouperStart == "(" || node is ProductionOrNode || grouperStart == "{" || grouperStart == "[").not()
            val validFinish = if (index < productionNodes.count() - 1) {
                val next = (productionNodes[index + 1])
                (validFinishArray.contains((next as? ProductionGrouperNode)?.grouper ?: "no")  || next is ProductionOrNode).not()
            }
            else {
                false
            }

            normalizedProductionNodes.add(node)
            if (validStart && validFinish){
                normalizedProductionNodes.add(ProductionConcatNode())
            }
        }

        val postfix = infixToPostfixList(normalizedProductionNodes)
        val tree = expressionTreeList(postfix)
        return tree
    }

    private fun getActionBlock(currentToken: TokenMatch) : String? {
        var paramBody : String?  = null
        var nextTokenMatch = currentToken
        var counter = 0
        do {
            if (nextTokenMatch.token.ident == CocolLangIdents.ActionParamBlockStart.ident) {
                counter++
                if (paramBody == null) paramBody = ""
            } else if (nextTokenMatch.token.ident == CocolLangIdents.ActionParamBlockEnd.ident){
                counter--
            }
            if (paramBody != null){
                paramBody += nextTokenMatch.match
            }

            //Move to next
            if (counter != 0) {
                nextTokenMatch = tokensSearchIterator.next()
            }
        }while (counter != 0)
        if (paramBody != null){
            paramBody = paramBody.substring(1, paramBody.count()-1)
        }



        return paramBody
    }

    private fun expressionTreeList(postfix: List<ProductionNode>): ProductionNode {
        val st: Stack<ProductionNode> = Stack<ProductionNode>()
        //We iterate
        for (i in postfix.indices) {
            //Get the lleter
            val prodNode = postfix[i]
            //If it is an operator we have to create the respective node
            val isSingleOp = (prodNode is ProductionOneOrZeroNode) || (prodNode is ProductionZeroOrModeNode)
            val isOperator = isSingleOp ||
                    (prodNode is ProductionOrNode) || (prodNode is ProductionConcatNode)
            if (isOperator) {
                when  {
                    //If it is a single operator we only pop one element from our stack and assign it
                    isSingleOp -> {
                        //Use the factory to create the single operator
                        if (st.empty()) {
                            throw Exception("El operador $prodNode esperaba una expresión a la cual se le pudiera asignar en el stack. 😬")
                        }
                        val a = st.pop()
                        prodNode.left = a
                    }
                    else -> {
                        //Use the factory to create the operator

                        if (st.empty()) {
                            throw Exception("El operador $prodNode esperaba una expresión a la cual se le pudiera asignar en el lado derecho. 👈🏻")
                        }
                        //Get the right value
                        val right = st.pop()
                        if (st.empty()) {
                            throw Exception("El operador $prodNode esperaba una expresión a la cual se le pudiera asignar en el lado izquierdo. 👉🏻")
                        }
                        //Get the left value
                        val left = st.pop()
                        //We set the left and right child
                        prodNode.left = left
                        prodNode.right = right
                    }
                }

                st.push(prodNode)
            } else {
                //if it is parenthesis
                if (prodNode is ProductionParenthesis){
                    //Use the factory to create the single operator
                    if (st.empty()) {
                        throw Exception("El operador $prodNode esperaba una expresión a la cual se le pudiera asignar en el stack. 😬")
                    }
                    val a = st.pop()
                    prodNode.left = a
                }
                st.push(prodNode)
            }
        }
        if (st.count() > 1){
            throw Exception("El grafo de producciones no fue resuelto de manera correcta")
        }
        return st.pop()
    }

    override fun infixToPostfixList(exp: List<ProductionNode>): List<ProductionNode> {
        val result = mutableListOf<ProductionNode>()
        val stack = Stack<ProductionNode>()
        val groupStarters = listOf("(", "[", "{")
        for (element in exp) {
            val isOperator = element is ProductionOrNode || element is ProductionConcatNode
            val isGrouper = element is ProductionGrouperNode
            when {
                !isOperator && !isGrouper -> result.add(element)
                element is ProductionGrouperNode && groupStarters.contains(element.grouper) -> {
                    stack.push(element)
                }
                element is ProductionGrouperNode && !groupStarters.contains(element.grouper) -> {
                    when(element.grouper){
                        ")" -> {
                            val popped = resolveClosingTag("(", stack, result)
                            resolveElement(
                                ProductionParenthesis().apply {
                                    //Set the semantic body
                                    startSemantic = popped?.semanticBody
                                    semanticBody = element.semanticBody
                                },
                                stack,
                                result
                            )
                        }
                        "}" -> {
                            val popped = resolveClosingTag("{", stack, result)
                            resolveElement(
                                ProductionZeroOrModeNode().apply {
                                    //Set the semantic body
                                    startSemantic = popped?.semanticBody
                                     semanticBody = element.semanticBody
                                },
                                stack,
                                result
                            )
                        }
                        else -> { // ]
                            val popped = resolveClosingTag("[", stack, result)
                            resolveElement(
                                ProductionOneOrZeroNode().apply {
                                    //Set the semantic body
                                    startSemantic = popped?.semanticBody
                                    semanticBody = element.semanticBody
                                },
                                stack,
                                result
                            )
                        }
                    }
                }
                else -> {
                    resolveElement(element, stack, result)
                }
            }
        }

        //We finish closing the parenthesis
        while (!stack.isEmpty()) {
            val peek = stack.peek() as? ProductionGrouperNode
            if (peek?.grouper == "(") throw Exception("No se cerro parentesis 😤")
            if (peek?.grouper == "{") throw Exception("No se cerro coso { 😤")
            if (peek?.grouper == "[") throw Exception("No se cerro coso [ 😤")
            result.add(stack.pop())
        }
        return result
    }

    private fun resolveElement(element: ProductionNode, stack: Stack<ProductionNode>, result: MutableList<ProductionNode>){
        //We get the precedence
        val cPrec = precedence(element)
        //While stack is not empty and the precedence of the element is lower than the head of the stack
        //We will pop the value and add it to result
        while (!stack.isEmpty() && cPrec <= precedence(stack.peek())
        ) {
            result.add(stack.pop())
        }
        stack.push(element)
    }

    private fun resolveClosingTag(symbol: String, stack: Stack<ProductionNode>, result: MutableList<ProductionNode>): ProductionNode? {

        //Stack is empty means the ( was not fund
        if (stack.isEmpty()) {
            throw java.lang.Exception("No hubo $symbol encontrado 😞")
        }
        while (!stack.isEmpty() &&
            (stack.peek() as? ProductionGrouperNode)?.grouper != symbol
        ) {
            result.add(stack.pop())
        }
        //After keep searching we dont have a ( to pop it was not even started
        if (stack.isEmpty()) {
            throw java.lang.Exception("No hubo $symbol encontrado 😭")
        }


        return stack.pop()

    }

    override fun precedence(char: ProductionNode): Int {
        return when(char){
            is ProductionZeroOrModeNode, is ProductionOneOrZeroNode, is ProductionParenthesis -> 3
            is ProductionConcatNode -> 2
            is ProductionOrNode -> 1
            else -> -1
        }
    }
}