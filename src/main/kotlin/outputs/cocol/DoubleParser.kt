package cocol.productions.template

import tokens.TokenExpression
import tokens.TokenMatch


class DoubleParser(
    val tokens: List<TokenMatch>
) {
    inner class Composition<T>(
        var value: T
    )

    val iterator = tokens.listIterator()

    private var currentToken: TokenMatch
    private var nextTokenMatch: TokenMatch

    init {
        currentToken = TokenMatch("", TokenExpression(""))
        nextTokenMatch = TokenMatch("", TokenExpression(""))
        if (tokens.isNotEmpty()) {
            nextTokenMatch = iterator.next()
        }
    }

    fun moveToken() {
        currentToken = nextTokenMatch
        if (iterator.hasNext()) {
            nextTokenMatch = iterator.next()
        }
    }

    fun checkNextTokenIs(ident: String) {
        if (nextTokenMatch.token.ident != ident) {
            println("El token debía de ser un $ident se recibio ${nextTokenMatch.token.ident} por el valor '${nextTokenMatch.match}'")
        }
        moveToken()

    }

    fun parse() {
        Expr()

    }

    fun Stat() {
        val comp = Composition<Double>(0.0);

        Expression(comp)
        println("Resultado: ${comp.value}");
    }

    fun Expr() {


        while (listOf<String>("raw -", "number", "decnumber", "raw (").contains(nextTokenMatch.token.ident)) {

            Stat()

            checkNextTokenIs("raw ;")



            while (listOf<String>("white").contains(nextTokenMatch.token.ident)) {

                checkNextTokenIs("white")


            }


        }


        while (listOf<String>("white").contains(nextTokenMatch.token.ident)) {

            checkNextTokenIs("white")


        }


        checkNextTokenIs("raw .")


    }

    fun Number(result: Composition<Double>) {


        when {
            nextTokenMatch.token.ident == "number" -> {
                checkNextTokenIs("number")

            }

            nextTokenMatch.token.ident == "decnumber" -> {
                checkNextTokenIs("decnumber")

            }
            else -> {
                println("No se pudo resolver ni una de las condiciónes para el valor ${nextTokenMatch.match}.")
            }

        }


        result.value = currentToken.match.toDouble();
    }

    fun Expression(result: Composition<Double>) {
        val result1 = Composition<Double>(0.0);
        val result2 = Composition<Double>(0.0);

        Term(result1)


        while (listOf<String>("raw +", "raw -").contains(nextTokenMatch.token.ident)) {

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

    fun Factor(result: Composition<Double>) {
        val sign = Composition<Double>(1.0);

        if (listOf<String>("raw -").contains(nextTokenMatch.token.ident)) {

            checkNextTokenIs("raw -")
            sign.value = -1.0;

        }


        when {
            nextTokenMatch.token.ident == "number" || nextTokenMatch.token.ident == "decnumber" -> {
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


        result.value *= sign.value;

    }

    fun Term(result: Composition<Double>) {
        val result1 = Composition<Double>(0.0);
        val result2 = Composition<Double>(0.0);

        Factor(result1)


        while (listOf<String>("raw *", "raw /").contains(nextTokenMatch.token.ident)) {

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
        result.value = result1.value;

    }
}