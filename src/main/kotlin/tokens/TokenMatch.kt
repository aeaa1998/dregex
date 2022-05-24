package tokens

class TokenMatch(
    val match: String,
    val token: TokenExpression
){
    override fun toString(): String {
        return "Token ${token.ident},Match: ${match}"
    }
}