package cocol.productions.template

import tokens.TokenExpression
import tokens.TokenMatch



class TemplateNameHere(
    val tokens: List<TokenMatch>
) {
    inner class Composition<T>(
        var value: T
    )

    val iterator = tokens.listIterator()

    private var currentToken : TokenMatch
    private var nextTokenMatch : TokenMatch

    init {
        currentToken = TokenMatch("", TokenExpression(""))
        nextTokenMatch = TokenMatch("", TokenExpression(""))
        if(tokens.isNotEmpty()) {
            nextTokenMatch = iterator.next()
        }
    }

    fun moveToken(){
        currentToken = nextTokenMatch
        if (iterator.hasNext()){
            nextTokenMatch = iterator.next()
        }
    }

    fun checkNextTokenIs(ident: String){
        if(nextTokenMatch.token.ident != ident){
            println("El token debía de ser un $ident se recibio ${nextTokenMatch.token.ident} por el valor '${nextTokenMatch.match}'")
        }else{
            moveToken()
        }
    }

    fun parse(){
        //root_function

    }

    //define_functions
}