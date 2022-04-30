package cocol.tokens

import cocol.CocolOperatorClass
import cocol.StringNormalizer
import cocol.assignKeyWord
import cocol.characters.CocolTextValueGetter
import dregex.OperatorNodeFactory
import dregex.RegexExpression
import dregex.SingleOperatorNodeFactory
import dregex.WordNode
import tokens.CoCoBoiCharacterSet
import tokens.TokenExpression
import tokens.TokenExpressionType
import tokens.TokenMatches
import utils.CocolLangIdents
import utils.Constants
import utils.Postfixable
import java.lang.Exception
import java.util.*

class CocolTokenReader(
    private val tokensSearchIterator: MutableIterator<TokenMatches>,
    private val identToken: TokenMatches,
    private val characters: List<CoCoBoiCharacterSet>,
    private val tokens: List<TokenExpression>
)  {
    private var excepted = false
    fun read(): TokenExpression {
        val charValue = getCharsValue()
        return TokenExpression(
            identToken.match,
            charValue.map{ it.second }.joinToString(""),
            TokenExpressionType.Normal
        ).apply {
            this.exceptKeywords = excepted
        }
    }

    private fun getCharsValue(): MutableList<Pair<String, String>> {
        if (identToken.token.ident.toLowerCase() != "ident"){
            throw Exception("La declaración deberia empezar con un ident")
        }
        if (tokensSearchIterator.hasNext().not()) throw Exception("Asignación esperada")
        val assign = tokensSearchIterator.next()
        if(assign.token.ident.toLowerCase() != assignKeyWord.ident)
            throw Exception("Se esperaba '=' despues de la declaracion del ident se recibio : ${assign.token.ident}")

        var declarationCompleted = false
        val charValue: MutableList<Pair<String, String>> = mutableListOf()
        while (!declarationCompleted){
            if (tokensSearchIterator.hasNext().not()) throw Exception("Declaración de charset no completada")

            val tokenMatch = tokensSearchIterator.next()
            when(tokenMatch.token.ident){
                "ident" -> {
                    //First we search on the characters
                    val charSet = characters.firstOrNull { it.ident ==  tokenMatch.match}
                    if (charSet != null){
                        charValue.add(Pair(tokenMatch.token.ident, "(${charSet.representation})"))
                    }else{
                        //If it was null we search on tokens
                        val tokenIdent = tokens.firstOrNull { it.ident ==  tokenMatch.match}
                        requireNotNull(tokenIdent){
                            "El token ${identToken.match} esta utilizando ${tokenMatch.match} que no ha sido definido"
                        }
                        //Set the expression with a group
                        charValue.add(Pair(tokenMatch.token.ident, "(${tokenIdent._expression})"))
                    }
                }

                "operator" -> {
                    val operator = tokenMatch.match
                    val operatorCocol = CocolOperatorClass.fromString(operator)
                    val validOperators = listOf(CocolOperatorClass.Or, CocolOperatorClass.Finish, CocolOperatorClass.Except)
                    if (validOperators.contains(operatorCocol).not()){
                        throw Exception("$operator not valid in tokens context")
                    }
                    if (operatorCocol == CocolOperatorClass.Finish){
                        declarationCompleted = true
                    }else
                    if (operatorCocol == CocolOperatorClass.Except){
                        val exception = Exception("Except keywords debe de ir seguido de .")
                        if(tokensSearchIterator.hasNext().not()){
                            throw exception
                        }
                        val finalToken = tokensSearchIterator.next()
                        val finalOperator = CocolOperatorClass.fromString(finalToken.match)
                        if (finalOperator != CocolOperatorClass.Finish){
                            throw exception
                        }
                        excepted = true
                        declarationCompleted = true
                    }
                    else{
                        charValue.add(Pair(tokenMatch.token.ident, operator))
                    }

                }
                "grouper" -> {
                    charValue.add(
                        Pair(
                            tokenMatch.token.ident,
                            tokenMatch.match
                        )
                    )
                }
                "string" -> {
                    val char = tokenMatch.match
                    val charNormalized = CocolTextValueGetter(char).get()
                    charValue.add(
                        Pair(
                            tokenMatch.token.ident,
//                            Constants.kotlinScriptManger.eval(char).toString().map {
//                                "\\$it"
//                            }.joinToString("")
                            "(${StringNormalizer(charNormalized).normalize().toString().map {
                                "\\$it"
                            }.joinToString("")})"

                        )
                    )
                }
                CocolLangIdents.Character.ident -> {
                    val char = tokenMatch.match
                    val charNormalized = CocolTextValueGetter(char).get()
                    charValue.add(
                        Pair(
                            tokenMatch.token.ident,
                            "(${StringNormalizer(charNormalized).normalize().toString().map {
                                "\\$it"
                            }.joinToString("")})"
//                            Constants.kotlinScriptManger.eval(char).toString().map {
//                                "\\$it"
//                            }.joinToString("")
                        )
                    )
                }
                else -> {
                    throw Exception("${tokenMatch.match} No es válido en este contexto.")
                }

            }

        }
        return charValue
    }

}