package dregex

import java.util.*


class DRegex(
    val regexString: String
) {
    private val singleOperators: List<String> = listOf("+", "*", "?")
    private val operators: List<String> = listOf("|", ".")
    private val aggrupators: List<String> = listOf("(", ")")
    private val parenthesisStart = "("
    private val parenthesisEnd = ")"



    private fun returnFromParenthesis(substring: String) : Pair<Int, RegexExpression>{
        var parenthesisCounter = 0
        substring.forEachIndexed { index, char ->
            val letter = char.toString()
            if (letter == parenthesisStart){
                parenthesisCounter++
            }else if (letter == parenthesisEnd){
                parenthesisCounter--
            }
            if (parenthesisCounter == 0){
                val expression = substring.substring(startIndex = 1, endIndex = index)
                val regex = normalize(expression)
                //If there are still chars to check
                return if (index < substring.length - 1 && singleOperators.contains(substring[index+1].toString())){
                    val (indexSingleO, new) = recursiveSingleOperator(regex, substring.substring(startIndex = index+1))
                    Pair(indexSingleO+index+1, new)
                }else{
                    Pair(index, regex)
                }

            }
        }
        throw Exception("The parenthesis tag must be closed")
    }

    private fun precedence(ch: Char): Int {
        return when (ch) {
            '•', '|' -> return 1
            '*', '?', '+' -> return 2
            else -> (-1)
        }
    }

    fun isOperator(ch: Char): Boolean {
        return ch == '•' || ch == '|' || ch == '?' || ch == '*' || ch == '+'
    }

    fun expressionTree(postfix: String): RegexExpression {
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

    fun infixToPostfix(exp: String): String {
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

    private fun recursiveSingleOperator(leftNode: RegexExpression?, substring: String) : Pair<Int, RegexExpression>{
        val iterator = substring.iterator()
        val letter = iterator.next().toString()
        val operatorRegex = SingleOperatorNodeFactory.create(letter)
        require(leftNode != null)
        //We need require to not be null
        operatorRegex.setNode(leftNode)
        //We will set the operator regex as the last
        if (substring.count() == 1){
            return Pair(0, operatorRegex)
        }else{
            val nextLetter = iterator.next().toString()
            //If the next is also a single oprator return it with recursion
            return if (singleOperators.contains(nextLetter)){
                val (index, regex) = recursiveSingleOperator(operatorRegex, substring.substring(startIndex = 1))
                Pair(1+index, regex)
            }else{
                Pair(0, operatorRegex)
            }
        }
    }

    private fun normalize(regexString: String) : RegexExpression {
        var currentLeft : RegexExpression? = null
        var i = 0

        while(i < regexString.length) {
            val char = regexString[i]
            val letter = char.toString()
            when {
                i<regexString.length-1 && singleOperators.contains(regexString[i+1].toString()) -> {
                    val prev = WordNode(letter)
                    val (new, operatorRegex) =recursiveSingleOperator(prev, regexString.substring(startIndex = i+1))
                    currentLeft = if (currentLeft != null){
                        val concat = ConcatNode()
                        concat.setNode(currentLeft)
                        concat.setNode(operatorRegex)
                        concat
                    }else {
                        //We will set the operator regex as the last if there is none
                        operatorRegex
                    }
                    //Update i var
                    i += new + 1
                }
                operators.contains(letter) -> {
                    // Cant be the first node in the tree
                    require(currentLeft != null)
                    //Cant be the last
                    require(i < regexString.length -1)
                    val operatorNode = OperatorNodeFactory.create(letter)
                    operatorNode.setNode(currentLeft)
                    val nextRegex = regexString[i+1].toString()
                    if (nextRegex == parenthesisStart){
                        val (new, regex) = returnFromParenthesis(regexString.substring(startIndex = i+1))
                        //Set the right node
                        operatorNode.setNode(regex)
                        i += new + 1
                    }else if (i < regexString.length -2 && singleOperators.contains(regexString[i+2].toString())){
                        val scopedLeft = WordNode(nextRegex)
                        val (new, operatorRegex) =recursiveSingleOperator(scopedLeft, regexString.substring(startIndex = i+2))
                        //Update i var
                        operatorNode.setNode(operatorRegex)
                        i += new + 2
                    }else{
                        val scopedLeft = WordNode(nextRegex)
                        operatorNode.setNode(scopedLeft)
                        i++
                    }

                    currentLeft = operatorNode
                }
                letter == parenthesisStart -> {
                    val (new, regex) = returnFromParenthesis(regexString.substring(startIndex = i))
                    val prev = currentLeft
                    //If there is already something we concat else we
                    currentLeft = if (prev != null){
                        val concat = ConcatNode()
                        concat.setNode(prev)
                        concat.setNode(regex)
                        concat
                    }else{
                        regex
                    }
                    i += new
                    //The new I is the last from the parentesis
                }
                else -> {
                    val holder =WordNode(letter)
                    val prev = currentLeft
                    //If there is already something we concat else we
                    if (prev != null){
                        val concat = ConcatNode()
                        concat.setNode(prev)
                        concat.setNode(holder)

                        currentLeft = concat
                    }else{
                        currentLeft = holder
                    }
                }
            }

            i++
        }
        require(currentLeft != null)
        return currentLeft

    }

    fun normalizeStack() : String {
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
//        return normalize(regexString)
        return expressionTree(normalizeStack())
    }


}