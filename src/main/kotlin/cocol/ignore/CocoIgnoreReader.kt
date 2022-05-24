package cocol.ignore

import cocol.CocolOperatorClass
import cocol.StringNormalizer
import cocol.characters.CocolTextValueGetter
import tokens.CoCoBoiCharacterSet
import tokens.TokenExpression
import tokens.TokenExpressionType
import tokens.TokenMatch
import utils.CocolLangIdents
import utils.Postfixable
import java.lang.Exception
import java.util.*

class CocoIgnoreReader(
    private val tokensSearchIterator: MutableIterator<TokenMatch>,
    private val characters: List<CoCoBoiCharacterSet>
) : Postfixable<Pair<String, String>, String> {
    fun read(): TokenExpression {
        val ignoreValue = getIgnoreValue()
        val postfix = infixToPostfixList(ignoreValue)
        val resolvedValue = resolveCharacterValue(postfix)
        val escapedValues = resolvedValue.second.map { char ->
            //Escape every value
            "\\$char"
        }

        return TokenExpression(
            "whitespace",
            escapedValues.joinToString("|"),
            TokenExpressionType.Ignore
        )
    }
    private fun resolveCharacterValue(postfix: List<Pair<String, String>>): Pair<String, String> {
        val st: Stack<Pair<String, String>> = Stack<Pair<String, String>>()
        //We iterate
        for (i in postfix.indices) {
            //Get the lleter
            val letter = postfix[i]
            //If it is an operator we have to create the respective node
            val match = postfix[i].second
            val ident = postfix[i].first
            if (CocolOperatorClass.isOperator(match)) {
                val operator = CocolOperatorClass.fromString(match)
                //Use the factory to create the operator
                if (st.empty()) {
                    throw Exception("El operador $letter esperaba una expresión a la cual se le pudiera operar en el lado derecho. 👈🏻")
                }
                //Get the right value
                val right = st.pop()
                if (st.empty()) {
                    throw Exception("El operador $letter esperaba una expresión a la cual se le pudiera operador en el lado izquierdo. 👉🏻")
                }
                //Get the left value
                val left = st.pop()

                //In case it is minus
                if (operator == CocolOperatorClass.Minus){
                    st.push(Pair("sanitized", left.second.filter { char ->
                        //We keep only the chars in left that are not in right
                        !right.second.contains(char)
                    }))
                }else if (operator == CocolOperatorClass.Plus) {
                    st.push(Pair("sanitized", left.second+right.second))
                }else if (operator == CocolOperatorClass.Range){
                    if (left.first.lowercase() != CocolLangIdents.Character.ident || right.first.lowercase() != CocolLangIdents.Character.ident){
                        throw Exception("El operador .. require que ambos valores sean de tipo char")
                    }
                    st.push(
                        Pair(
                            "sanitized",
                            left.second.first().toChar().rangeTo(right.second.first()).joinToString("")
                        )
                    )
                }else{
                    throw Exception("El operador $match no esta soportado")
                }
            } else {
                st.push(letter)
            }
        }


        return st.pop()
    }

    private fun resolveElement(element: Pair<String, String>, stack: Stack<Pair<String, String>>, result: MutableList<Pair<String, String>>){
        //We get the precedence
        val cPrec = if (element.first == CocolLangIdents.Operator.ident) precedence(element.second) else -1
        //While stack is not empty and the precedence of the element is lower than the head of the stack
        //We will pop the value and add it to result
        while (!stack.isEmpty() && cPrec <= precedence(stack.peek().second)
        ) {
            result.add(stack.pop())
        }
        stack.push(element)
    }
    override fun infixToPostfixList(exp: List<Pair<String, String>>): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        val stack = Stack<Pair<String, String>>()
        for (elementPair in exp) {
            val element = elementPair.second
            val ident = elementPair.first
            when {
                ident != CocolLangIdents.Operator.ident -> result.add(elementPair)
                else -> {
                    resolveElement(elementPair, stack, result)
                }
            }
        }

        while (!stack.isEmpty()) {
            result.add(stack.pop())
        }

        return result
    }

    override fun precedence(char: String): Int {
        val operator = CocolOperatorClass.fromString(char)
        return CocolOperatorClass.ignoreScopePrecedence[operator] ?: -1
    }

    private fun getIgnoreValue(): MutableList<Pair<String, String>> {
        var declarationCompleted = false
        val ignoreValue: MutableList<Pair<String, String>> = mutableListOf()
        while (!declarationCompleted){
            if (tokensSearchIterator.hasNext().not()) throw Exception("Declaración de los valores de ignore no completada")

            val tokenMatch = tokensSearchIterator.next()
            when(tokenMatch.token.ident){
                CocolLangIdents.WhiteSpace.ident -> {
                    continue
                }
                CocolLangIdents.Ident.ident -> {
                    val charSet = characters.firstOrNull { it.ident ==  tokenMatch.match}
                    requireNotNull(charSet){
                        "No se ha declarado ${tokenMatch.match} antes de utilizarlo"
                    }
                    ignoreValue.add(Pair(tokenMatch.token.ident, charSet.value.joinToString("")))
                }
                CocolLangIdents.Operator.ident -> {
                    val operator = tokenMatch.match
                    val operatorCocol = CocolOperatorClass.fromString(operator)
                    val validOperators = listOf(CocolOperatorClass.Minus, CocolOperatorClass.Plus, CocolOperatorClass.Finish, CocolOperatorClass.Range)
                    if (validOperators.contains(operatorCocol).not()){
                        throw Exception("$operator not valid in ignore context")
                    }
                    if (operatorCocol == CocolOperatorClass.Finish){
                        declarationCompleted = true
                    }else{
                        ignoreValue.add(Pair(tokenMatch.token.ident, operator))
                    }

                }
                CocolLangIdents.StringIdent.ident -> {
                    val char = tokenMatch.match
                    ignoreValue.add(Pair(tokenMatch.token.ident, StringNormalizer(char).normalize()))
                }
                CocolLangIdents.Character.ident -> {
                    val char = tokenMatch.match
                    val norm = StringNormalizer(CocolTextValueGetter(char).get()).normalize()
                    ignoreValue.add(Pair(tokenMatch.token.ident, norm))
                }
                else -> {
                    throw Exception("${tokenMatch.match} No es válido en este contexto de ignore.")
                }

            }

        }
        return ignoreValue
    }
}