package dregex

import java.util.*


class DRegex(
    private val regexString: String
) {
    lateinit var expression : RegexExpression
    private val singleOperators: List<String> = listOf("+", "*", "?")

    private fun precedence(char: Char): Int {
        return when (char) {
            '|' -> 1
            '•' -> return 2
            '*', '?', '+' -> return 3
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
                    //If it is a single operator we only pop one element from our stack and assign it
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
        var result = ""
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


    private fun normalizeStack(): String {
        var normalizedString = ""

        regexString.forEachIndexed { index, char ->
            val firstIsLetterOrDigitOrClosingParenthesis = (char == '(' || char == '|').not()
            val nextIsLetterOrDigitOrOpeningParenthesis =
                if (index < regexString.length - 1) (regexString[index+1] == ')' || isOperator(regexString[index+1])).not() else false
            normalizedString += char
            if (firstIsLetterOrDigitOrClosingParenthesis && nextIsLetterOrDigitOrOpeningParenthesis) {
                normalizedString += "•"
            }
        }

        return infixToPostfix(normalizedString)

    }


    fun build() : RegexExpression {
        expression =  expressionTree(normalizeStack())
        return expression
    }

    fun getDirectExpression() : RegexExpression {
        if (!this::expression.isInitialized){
            build()
        }
        return DirectRegex(this@DRegex.expression)

    }


}