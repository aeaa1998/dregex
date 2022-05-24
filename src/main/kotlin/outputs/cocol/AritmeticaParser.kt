package cocol.productions.template

import tokens.TokenExpression
import tokens.TokenMatch



class AritmeticaParser(
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
        }
        moveToken()

    }

    fun parse(){
        Expr()

    }

                fun Stat(){
                val composition = Composition<Double>(0.0)
                
                Expression(composition)
println(composition.value.toString());
            }

            fun Expr(){
                
                
                                    
        while(listOf<String>("raw -", "number", "raw (").contains(nextTokenMatch.token.ident)){
                
                Stat()

checkNextTokenIs("raw ;")


                
            }
            
        checkNextTokenIs("raw .")

        
            }

           fun Number(result: Composition<Double>){
               
               
               checkNextTokenIs("number")
result.value = currentToken.match.toDouble()
           }

            fun Expression(result: Composition<Double>){
                val result1 = Composition<Double>(0.0); val result2 = Composition<Double>(0.0);
                
                        Term(result1)

                    
        while(listOf<String>("raw +", "raw -").contains(nextTokenMatch.token.ident)){
                println("ANTES DE SUMAR");
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
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                
            }
            result.value = result1.value;
        
            }

            fun Factor(result: Composition<Double>){
                val signo=Composition<Int>(1);
                
                                    if(listOf<String>("raw -").contains(nextTokenMatch.token.ident)){
            
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
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
            result.value*=signo.value;
        
            }

            fun Term(result: Composition<Double>){
                val result1 = Composition<Double>(0.0); val result2 = Composition<Double>(0.0);
                
                        Factor(result1)

                    
        while(listOf<String>("raw *", "raw /").contains(nextTokenMatch.token.ident)){
                
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
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                
            }
            result.value=result1.value;
        
            }
}