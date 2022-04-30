package utils

interface Postfixable<ListContent, PrecedenceCheck> {
    fun infixToPostfixList(exp: List<ListContent>): List<ListContent>
    fun precedence(char: PrecedenceCheck): Int
}