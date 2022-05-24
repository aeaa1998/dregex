package cocol.productions.template

import tokens.TokenExpression
import tokens.TokenMatch



class AritmeticaParserA(
    val tokens: List<TokenMatch>
) {
    inner class Composition<T>(
        var value: T
    )

    val iterator = tokens.listIterator()

    private var currentToken : TokenMatch
    private var nextTokenMatch : TokenMatch

    init {
        when {
            tokens.count() > 2 -> {
                currentToken = iterator.next()
                nextTokenMatch = iterator.next()
            }
            tokens.isNotEmpty() -> {
                currentToken = iterator.next()
                nextTokenMatch = currentToken
            }
            else -> {
                currentToken = TokenMatch("", TokenExpression(""))
                nextTokenMatch = TokenMatch("", TokenExpression(""))
            }
        }
    }

    fun checkNextTokenIs(ident: String){
        require(currentToken.token.ident == ident){
            "El token debía de ser un $ident se recibio ${currentToken.token.ident} por el valor ${currentToken.match}"
        }
        currentToken = nextTokenMatch
        if (iterator.hasNext()){
            nextTokenMatch = iterator.next()
        }

    }

    fun parse(){
        Expr()
    }

    fun Stat(){
        val composition = Composition<Double>(0.0)

        Expression(composition)
        println(composition.value.toString())
    }

    fun Expr(){
        while(listOf("raw -", "number", "raw (").contains(nextTokenMatch.token.ident)){
            Stat()
            checkNextTokenIs("raw ;")
        }
        checkNextTokenIs("raw .")
    }

   fun Number(result: Composition<Double>){
               
               
       checkNextTokenIs("number")
        result.value = currentToken.match.toDouble()
   }

    fun Expression(result: Composition<Double> ){
        val result1 = Composition<Double>(0.0); val result2 = Composition<Double>(0.0);
        Term(result1)
        while(listOf("raw +", "raw -").contains(nextTokenMatch.token.ident)){
            when {
                nextTokenMatch.token.ident == "raw +" -> {
                            checkNextTokenIs("raw +")

                Term(result2)
                result1.value += result2.value;
        
                }
                nextTokenMatch.token.ident == "raw -" -> {
                    checkNextTokenIs("raw -")
                    Term(result2)
                    result1.value -= result2.value;

                }
                
                else -> {
                    println("Error")
                }
            }
            
            
                result.value = result1.value;
            }
        
    }

    fun Factor(result: Composition<Double> ){
            val signo=Composition<Int>(1);

            if(listOf("raw -").contains(nextTokenMatch.token.ident)){
            checkNextTokenIs("raw -")
            signo.value = -1;

            }
                                when {
                nextTokenMatch.token.ident == "number" -> {
                    Number(result)

                }
                
                nextTokenMatch.token.ident == "raw (" -> {
                            checkNextTokenIs("raw (")

Expression(result)


        checkNextTokenIs("raw )")

        
                }
                
                else -> {
                    println("Error")
                }
            }
            
            
            result.value*=signo.value;
        
            }

            fun Term(result: Composition<Double>){

                val result1 = Composition<Double>(0.0)
                val result2 = Composition<Double>(0.0)
                
                        Factor(result1)

                while(listOf("raw *", "raw /").contains(nextTokenMatch.token.ident)){
                            when {
                nextTokenMatch.token.ident == "raw *" -> {
                            checkNextTokenIs("raw *")

        Factor(result2)
result1.value *= result2.value;
        
                }
                
                nextTokenMatch.token.ident == "raw /" -> {
                            checkNextTokenIs("raw /")

        Factor(result2)
result1.value /= result2.value;
        
                }
                
                else -> {
                    println("Error")
                }
            }
            
            
                result.value=result1.value;
            }
        
            }
}