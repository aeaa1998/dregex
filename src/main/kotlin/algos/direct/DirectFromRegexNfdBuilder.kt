package algos.direct

import automatons.NFD
import dregex.TokenOrGenerator
import tokens.TokenExpression

class DirectFromRegexNfdBuilder(
    val tokens: List<TokenExpression>,
    val keywords: List<TokenExpression>,
    val ignore: TokenExpression?
) {
    fun buildNfd(): NFD<DirectRegexSimplified> {
        val final = (keywords+tokens).toMutableList()
        val tokenRegex = TokenOrGenerator(final, ignore)
            .generate()
        final.run {
            if (ignore != null){
                add(ignore)
            }
        }
        val regexString = final.map { "(${it._expression})" }.joinToString(" --------- ")
        val dir = DirectFromRegex(regexString,tokenRegex).build()
        return dir.nfd
    }
}