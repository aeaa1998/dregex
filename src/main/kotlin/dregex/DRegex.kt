package dregex

import java.util.*


class DRegex(
    private val regexString: String
) {
    private val singleOperators: List<String> = listOf("+", "*", "?")

    private fun precedence(ch: Char): Int {
        return when (ch) {
            '•', '|' -> return 1
            '*', '?', '+' -> return 2
            else -> (-1)
        }
    }

    private fun isOperator(ch: Char): Boolean {
        return ch == '•' || ch == '|' || ch == '?' || ch == '*' || ch == '+'
    }

    private fun expressionTree(postfix: String): RegexExpression {
        val st: Stack<RegexExpression> = Stack<RegexExpression>()

        for (i in postfix.indices) {
            val letter = postfix[i].toString()
            if (isOperator(postfix[i])) {
                val nodeOp: RegexExpression
                when  {
                    singleOperators.contains(letter) -> {
                        nodeOp = SingleOperatorNodeFactory.create(letter)
                        val a = st.pop()
                        nodeOp.setNode(a)
                    }
                    else -> {
                        nodeOp = OperatorNodeFactory.create(letter)
                        val right = st.pop()
                        val left = st.pop()
                        nodeOp.setNode(left)
                        nodeOp.setNode(right)
                    }
                }

                st.push(nodeOp)
            } else {
                st.push(WordNode(letter))
            }
        }
        return st.pop()
    }

    private fun infixToPostfix(exp: String): String {
        var result: String = ""
        val stack = Stack<Char>()
        for (element in exp) {
            when {
                Character.isLetterOrDigit(element) -> result += element
                element == '(' -> stack.push(element)
                element == ')' -> {
                    while (!stack.isEmpty() &&
                        stack.peek() != '('
                    ) result += stack.pop()
                    stack.pop()
                }
                else -> {
                    val cPrec = precedence(element)
                    while (!stack.isEmpty() && cPrec <= precedence(stack.peek())
                    ) {
                        result += stack.pop()
                    }
                    stack.push(element)
                }
            }
        }


        while (!stack.isEmpty()) {
            if (stack.peek() == '(') throw Exception("No se cerro parentesis")
            result += stack.pop()
        }
        return result
    }


    private fun normalizeStack() : String {
        var normalizedString = ""

        regexString.forEachIndexed { index, char ->
            val firstIsLetterOrDigitOrClosingParenthesis = char.isLetterOrDigit()  || char == ')' || char == '*' || char == '?' || char == '+'
            val nextIsLetterOrDigitOrOpeningParenthesis = if (index < regexString.length-1) regexString[index+1].isLetterOrDigit() || char == '(' else false
            normalizedString+=char
            if (firstIsLetterOrDigitOrClosingParenthesis && nextIsLetterOrDigitOrOpeningParenthesis){
                normalizedString+="•"
            }

        }
        val posfix = infixToPostfix(normalizedString)

        return posfix

    }


    fun build() : RegexExpression {
        return expressionTree(normalizeStack())
    }


}