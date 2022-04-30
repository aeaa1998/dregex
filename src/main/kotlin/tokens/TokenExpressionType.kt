package tokens

enum class TokenExpressionType {
    Normal, Keyword, Ignore;

    fun isKeyWord() = this == Keyword
}