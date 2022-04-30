package algos.direct

import automatons.NFD
import tokens.TokenExpression
import tokens.TokenExpressionType
import tokens.TokenMatches
import java.lang.Exception

class DirectFromRegexTokenDetector(
    val nfd: NFD<DirectRegexSimplified>
) {
    fun getTokens(text: String): MutableList<TokenMatches> {
        var previous = nfd.initialState
        var pointer = nfd.initialState
        var currentText: String = ""
        val tokens: MutableList<TokenMatches> = mutableListOf()
        var index = 0
        var lastToken: TokenExpression? = null
        var lastValidIndex: Int = -1
        while (index < text.length){
            val char = text[index]
            val isFinal = nfd.isFinal(pointer)
            if (isFinal) {
                currentText.lastOrNull()?.let { last ->
                    val token = nfd.transitionTokens[previous.secondaryId]?.get(last.toString())
                    if (token != null) {
                        lastToken = token
                        lastValidIndex = index
                    }
                }
            }

            val newState = nfd.move(pointer, char.toString())
            if (newState == null){
                if (lastToken != null && lastValidIndex != -1){
                    if (lastToken!!.type != TokenExpressionType.Ignore){
                        tokens.add(TokenMatches(currentText, lastToken!!))
                    }
                    //Reset
                    previous = nfd.initialState
                    pointer = nfd.initialState
                    currentText = ""
                    index = lastValidIndex
                    lastValidIndex = -1
                }else{
                    println("Error Léxico: Se encontro un error lexico al leer el carácter $char en el texto actual $currentText")
//                    throw Exception("Error Léxico")
                    //Restart
                    previous = nfd.initialState
                    pointer = nfd.initialState
                    currentText = ""
                    lastValidIndex = -1
                    index++
                }
            }
            else {
                previous = pointer
                pointer = newState
                currentText += char
                index++
            }
        }

        if (currentText.isNotEmpty()){
            if (nfd.isFinal(pointer)){
                currentText.lastOrNull()?.let { last ->
                    val token = nfd.transitionTokens[previous.secondaryId]?.get(last.toString())
                    if (token != null) {
                        tokens.add(TokenMatches(currentText, token))
                    } else {
                        println("Error Léxico: Se encontro un error lexico No existe un token para la palabra $currentText")
//                    throw Exception("Error Léxico")
                    }
                }
            }else{
                println("Error Léxico: Se encontro un error lexico No existe un token para la palabra $currentText no es un token final")
            }
        }
        return tokens
    }
}