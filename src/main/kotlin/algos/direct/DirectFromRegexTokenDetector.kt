package algos.direct

import automatons.NFD
import tokens.TokenExpression
import tokens.TokenExpressionType
import tokens.TokenMatch

class DirectFromRegexTokenDetector(
    val nfd: NFD<DirectRegexSimplified>
) {
    fun getTokens(text: String): MutableList<TokenMatch> {
        var previous = nfd.initialState
        var pointer = nfd.initialState
        var currentText: String = ""
        val tokens: MutableList<TokenMatch> = mutableListOf()
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
                        tokens.add(TokenMatch(currentText, lastToken!!))
                    }
                    //Reset
                    previous = nfd.initialState
                    pointer = nfd.initialState
                    currentText = ""
                    index = lastValidIndex
                    lastValidIndex = -1
                }else{
                    println("Error Léxico: Se encontro un error lexico al leer el carácter $char en el texto actual $currentText")
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
                        tokens.add(TokenMatch(currentText, token))
                    } else {
                        println("Error Léxico: Se encontro un error lexico No existe un token para la palabra $currentText")
                    }
                }
            }else{
                println("Error Léxico: Se encontro un error lexico No existe un token para la palabra $currentText no es un token final")
            }
        }
        return tokens
    }
}