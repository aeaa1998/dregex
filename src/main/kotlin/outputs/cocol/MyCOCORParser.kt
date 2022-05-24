package cocol.productions.template

import tokens.TokenExpression
import tokens.TokenMatch



class MyCOCORParser(
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
        MyCOCOR()

    }

                fun MyCOCOR(){
                val compilerName = Composition<String>("");
					  val endName = Composition<String>("");
                
                                                        checkNextTokenIs("raw COMPILER")

        Ident(compilerName)
println("Nombre inicial del compilador ${compilerName.value}");
        
        if(listOf<String>("startcode").contains(nextTokenMatch.token.ident)){

    Codigo()

    
}

        
        Body()

        
        checkNextTokenIs("raw END")

        
        Ident(endName)
println("Nombre Final del Compilador: ${endName.value}");
        
            }

            fun Keywords(){
                val keyName = Composition<String>("");
					  val stringValue = Composition<String>("");
    					  val counter = Composition<Int>(0);
                
                        checkNextTokenIs("raw KEYWORDS")
println("LEYENDO KEYWORDS");
                    
        while(listOf<String>("ident").contains(nextTokenMatch.token.ident)){
                
                                        Ident(keyName)
counter.value++;println("KeyWord ${counter.value}: ${keyName.value}");
        checkNextTokenIs("raw =")

        
        String(stringValue)

        
        checkNextTokenIs("raw .")

        
                
            }
            
        
            }

            fun SymbolProd(){
                val sV = Composition<String>("");
				  val IN = Composition<String>("");
                
                            
                        when {
                nextTokenMatch.token.ident == "string" || nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "raw ANY" -> {
                                when {
                nextTokenMatch.token.ident == "string" || nextTokenMatch.token.ident == "char" -> {
                                when {
                nextTokenMatch.token.ident == "string" -> {
                    String(sV)
println("String en Production: ${sV.value}");
                }
                
                nextTokenMatch.token.ident == "char" -> {
                    checkNextTokenIs("char")

                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                }
                
                nextTokenMatch.token.ident == "raw ANY" -> {
                    checkNextTokenIs("raw ANY")

                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                }
                
                nextTokenMatch.token.ident == "ident" -> {
                            Ident(IN)
println("Identificador en Production: ${IN.value}");
        if(listOf<String>("raw <").contains(nextTokenMatch.token.ident)){

    Atributos()

    
}

        
                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
            
            }

            fun Ident(sFound: Composition<String>){
                
                
                checkNextTokenIs("ident")
sFound.value = currentToken.match;
            }

            fun IgnoreCase(){
                
                
                                                    checkNextTokenIs("raw IGNORE")
            val identName = Composition<String>(""); val stringValue = Composition<String>("");
val identName2 = Composition<String>(""); val stringValue2 = Composition<String>("");

                    
                        when {
                nextTokenMatch.token.ident == "ident" || nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "charnumber" || nextTokenMatch.token.ident == "charinterval" -> {
                                when {
                nextTokenMatch.token.ident == "ident" -> {
                    Ident(identName)
println("Encontrado ident para ignorar ${identName.value}");
                }
                
                nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "charnumber" || nextTokenMatch.token.ident == "charinterval" -> {
                    Char()

                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                }
                
                nextTokenMatch.token.ident == "string" -> {
                    String(stringValue)
println("Encontrado string para ignorar ${stringValue.value}");
                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
            
        
                    
        while(listOf<String>("ident", "char", "charnumber", "charinterval", "string").contains(nextTokenMatch.token.ident)){
                
                            when {
                nextTokenMatch.token.ident == "ident" || nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "charnumber" || nextTokenMatch.token.ident == "charinterval" -> {
                                when {
                nextTokenMatch.token.ident == "ident" -> {
                    Ident(identName2)
println("Encontrado ident para ignorar ${identName.value}");
                }
                
                nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "charnumber" || nextTokenMatch.token.ident == "charinterval" -> {
                    Char()

                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                }
                
                nextTokenMatch.token.ident == "string" -> {
                    String(stringValue2)
println("Encontrado string para ignorar ${stringValue.value}");
                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                
            }
            
        
        checkNextTokenIs("raw .")

        
            }

            fun TokenTerm(){
                
                
                        TokenFactor()

            
while(listOf<String>("string", "char", "ident", "raw (", "raw [", "raw {").contains(nextTokenMatch.token.ident)){
        
        TokenFactor()

        
    }
    
        
            }

            fun Codigo(){
                
                
                                checkNextTokenIs("startcode")

            
while(listOf<String>().contains(nextTokenMatch.token.ident) || listOf<String>("ident", "string", "char", "charnumber", "charinterval", "nontoken", "startcode", "raw COMPILER", "raw END", "raw IGNORE", "raw .", "raw CHARACTERS", "raw =", "raw +", "raw -", "raw KEYWORDS", "raw TOKENS", "raw PRODUCTIONS", "raw EXCEPT", "raw |", "raw (", "raw )", "raw [", "raw ]", "raw {", "raw }", "raw ANY", "raw <", "raw >").contains(nextTokenMatch.token.ident)){
        
        moveToken()

        
    }
    
        
        checkNextTokenIs("endcode")

        
            }

            fun Characters(){
                val charName = Composition<String>("");
					   val counter = Composition<Int>(0);
                
                        checkNextTokenIs("raw CHARACTERS")
println("LEYENDO CHARACTERS");
                    
        while(listOf<String>("ident").contains(nextTokenMatch.token.ident)){
                
                                                Ident(charName)
counter.value++;println("Char Set ${counter.value}: ${charName.value}");
        checkNextTokenIs("raw =")

        
        CharSet()

        
                    
        while(listOf<String>("raw +", "raw -").contains(nextTokenMatch.token.ident)){
                
                            when {
                nextTokenMatch.token.ident == "raw +" -> {
                    checkNextTokenIs("raw +")

CharSet()


                }
                
                nextTokenMatch.token.ident == "raw -" -> {
                    checkNextTokenIs("raw -")

CharSet()


                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                
            }
            
        
        checkNextTokenIs("raw .")

        
                
            }
            
        
            }

            fun CharSet(){
                val IdentName = Composition<String>("");
				  val StringValue = Composition<String>("");
                
                            
                        when {
                nextTokenMatch.token.ident == "string" || nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "charnumber" || nextTokenMatch.token.ident == "charinterval" || nextTokenMatch.token.ident == "raw ANY" -> {
                                when {
                nextTokenMatch.token.ident == "string" || nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "charnumber" || nextTokenMatch.token.ident == "charinterval" -> {
                                when {
                nextTokenMatch.token.ident == "string" -> {
                    String(StringValue)

                }
                
                nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "charnumber" || nextTokenMatch.token.ident == "charinterval" -> {
                    Char()

                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                }
                
                nextTokenMatch.token.ident == "raw ANY" -> {
                    checkNextTokenIs("raw ANY")

                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                }
                
                nextTokenMatch.token.ident == "ident" -> {
                    Ident(IdentName)
println("Identificador en CharSet: ${IdentName.value}");
                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
            
            }

            fun String(sFound: Composition<String>){
                
                
                checkNextTokenIs("string")
sFound.value = currentToken.match;
            }

            fun Atributos(){
                
                
                                checkNextTokenIs("raw <")

                    
        while(listOf<String>("raw <").contains(nextTokenMatch.token.ident) || listOf<String>("ident", "string", "char", "charnumber", "charinterval", "nontoken", "startcode", "endcode", "raw COMPILER", "raw END", "raw IGNORE", "raw .", "raw CHARACTERS", "raw =", "raw +", "raw -", "raw KEYWORDS", "raw TOKENS", "raw PRODUCTIONS", "raw EXCEPT", "raw |", "raw (", "raw )", "raw [", "raw ]", "raw {", "raw }", "raw ANY").contains(nextTokenMatch.token.ident)){
                
                when {
    nextTokenMatch.token.ident == "raw <" -> {
        Generico()

    }
    
    else -> {
        moveToken()

    }
    
    
}


                
            }
            
        
        checkNextTokenIs("raw >")

        
            }

            fun Productions(){
                val counter = Composition<Int>(0);
                
                             checkNextTokenIs("raw PRODUCTIONS")
     val prodName = Composition<String>("");
println("LEYENDO PRODUCTIONS");
                    
        while(listOf<String>("ident").contains(nextTokenMatch.token.ident)){
                
                                                        Ident(prodName)
counter.value++;println("Production ${counter.value}: ${prodName.value}");
        if(listOf<String>("raw <").contains(nextTokenMatch.token.ident)){

    Atributos()

    
}

        
        checkNextTokenIs("raw =")

        
        if(listOf<String>("startcode").contains(nextTokenMatch.token.ident)){

    Codigo()

    
}

        
        ProductionExpr()

        
        checkNextTokenIs("raw .")

        
                
            }
            
        
            }

            fun ProdFactor(){
                
                
                                    
                        when {
                nextTokenMatch.token.ident == "string" || nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "raw ANY" || nextTokenMatch.token.ident == "ident" || nextTokenMatch.token.ident == "raw (" || nextTokenMatch.token.ident == "raw [" -> {
                                when {
                nextTokenMatch.token.ident == "string" || nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "raw ANY" || nextTokenMatch.token.ident == "ident" || nextTokenMatch.token.ident == "raw (" -> {
                                when {
                nextTokenMatch.token.ident == "string" || nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "raw ANY" || nextTokenMatch.token.ident == "ident" -> {
                    SymbolProd()

                }
                
                nextTokenMatch.token.ident == "raw (" -> {
                            checkNextTokenIs("raw (")

ProductionExpr()


        checkNextTokenIs("raw )")

        
                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                }
                
                nextTokenMatch.token.ident == "raw [" -> {
                            checkNextTokenIs("raw [")

ProductionExpr()


        checkNextTokenIs("raw ]")

        
                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                }
                
                nextTokenMatch.token.ident == "raw {" -> {
                            checkNextTokenIs("raw {")

ProductionExpr()


        checkNextTokenIs("raw }")

        
                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
            
        if(listOf<String>("startcode").contains(nextTokenMatch.token.ident)){

    Codigo()

    
}

        
            }

            fun TokenFactor(){
                
                
                            
                        when {
                nextTokenMatch.token.ident == "string" || nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "ident" || nextTokenMatch.token.ident == "raw (" || nextTokenMatch.token.ident == "raw [" -> {
                                when {
                nextTokenMatch.token.ident == "string" || nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "ident" || nextTokenMatch.token.ident == "raw (" -> {
                                when {
                nextTokenMatch.token.ident == "string" || nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "ident" -> {
                    SimbolToken()

                }
                
                nextTokenMatch.token.ident == "raw (" -> {
                            checkNextTokenIs("raw (")

TokenExpr()


        checkNextTokenIs("raw )")

        
                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                }
                
                nextTokenMatch.token.ident == "raw [" -> {
                            checkNextTokenIs("raw [")

TokenExpr()


        checkNextTokenIs("raw ]")

        
                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                }
                
                nextTokenMatch.token.ident == "raw {" -> {
                            checkNextTokenIs("raw {")

TokenExpr()


        checkNextTokenIs("raw }")

        
                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
            
            }

            fun ProdTerm(){
                
                
                        ProdFactor()

            
while(listOf<String>("string", "char", "raw ANY", "ident", "raw (", "raw [", "raw {").contains(nextTokenMatch.token.ident)){
        
        ProdFactor()

        
    }
    
        
            }

            fun TokenExpr(){
                
                
                        TokenTerm()

                    
        while(listOf<String>("raw |").contains(nextTokenMatch.token.ident)){
                
                checkNextTokenIs("raw |")

TokenTerm()


                
            }
            
        
            }

            fun ProductionExpr(){
                
                
                        ProdTerm()

                    
        while(listOf<String>("raw |").contains(nextTokenMatch.token.ident)){
                
                checkNextTokenIs("raw |")

ProdTerm()


                
            }
            
        
            }

            fun Char(){
                
                
                            
                        when {
                nextTokenMatch.token.ident == "char" || nextTokenMatch.token.ident == "charnumber" -> {
                                when {
                nextTokenMatch.token.ident == "char" -> {
                    checkNextTokenIs("char")

                }
                
                nextTokenMatch.token.ident == "charnumber" -> {
                    checkNextTokenIs("charnumber")

                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                }
                
                nextTokenMatch.token.ident == "charinterval" -> {
                    checkNextTokenIs("charinterval")

                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
            
            }

            fun ExceptKeyword(){
                
                
                checkNextTokenIs("raw EXCEPT")

checkNextTokenIs("raw KEYWORDS")


            }

            fun Generico(){
                
                
                                checkNextTokenIs("raw <")

            
while(listOf<String>().contains(nextTokenMatch.token.ident) || listOf<String>("ident", "string", "char", "charnumber", "charinterval", "nontoken", "startcode", "endcode", "raw COMPILER", "raw END", "raw IGNORE", "raw .", "raw CHARACTERS", "raw =", "raw +", "raw -", "raw KEYWORDS", "raw TOKENS", "raw PRODUCTIONS", "raw EXCEPT", "raw |", "raw (", "raw )", "raw [", "raw ]", "raw {", "raw }", "raw ANY", "raw <").contains(nextTokenMatch.token.ident)){
        
        moveToken()

        
    }
    
        
        checkNextTokenIs("raw >")

        
            }

            fun Body(){
                
                
                                                Characters()

        if(listOf<String>("raw IGNORE").contains(nextTokenMatch.token.ident)){

    IgnoreCase()

    
}

        
        if(listOf<String>("raw KEYWORDS").contains(nextTokenMatch.token.ident)){

    Keywords()

    
}

        
        Tokens()

        
        Productions()

        
            }

            fun Tokens(){
                val tokenName = Composition<String>("");
					  val counter = Composition<Int>(0);
                
                        checkNextTokenIs("raw TOKENS")
println("LEYENDO TOKENS");
                    
        while(listOf<String>("ident").contains(nextTokenMatch.token.ident)){
                
                                                Ident(tokenName)
counter.value++;println("Token ${counter.value}: ${tokenName.value}");
        checkNextTokenIs("raw =")

        
        TokenExpr()

        
        if(listOf<String>("raw EXCEPT").contains(nextTokenMatch.token.ident)){

    ExceptKeyword()

    
}

        
        checkNextTokenIs("raw .")

        
                
            }
            
        
            }

            fun SimbolToken(){
                val IdentName = Composition<String>("");
				  val StringValue = Composition<String>("");
                
                            
                        when {
                nextTokenMatch.token.ident == "string" || nextTokenMatch.token.ident == "char" -> {
                                when {
                nextTokenMatch.token.ident == "string" -> {
                    String(StringValue)

                }
                
                nextTokenMatch.token.ident == "char" -> {
                    checkNextTokenIs("char")

                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
                }
                
                nextTokenMatch.token.ident == "ident" -> {
                    Ident(IdentName)
println("Identificador en Token: ${IdentName.value}");
                }
                else -> {
    println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
}
                
            }
            
            
            
            }
}