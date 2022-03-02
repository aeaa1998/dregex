package dregex

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

//    private fun returnFromParenthesisExpression(substring: String) : Pair<Int, RegexExpression>{
//        var parenthesisCounter = 0
//        substring.forEachIndexed { index, char ->
//            val letter = char.toString()
//            if (letter == parenthesisStart){
//                parenthesisCounter++
//            }else if (letter == parenthesisEnd){
//                parenthesisCounter--
//            }
//            if (parenthesisCounter == 0){
//                val expression = substring.substring(startIndex = 1, endIndex = index)
//                val regex = normalize(expression)
//                //If there are still chars to check
//                return if (index < substring.length - 1 && singleOperators.contains(substring[index+1].toString())){
//                    val (indexSingleO, new) = recursiveSingleOperator(regex, substring.substring(startIndex = index+1))
//                    Pair(indexSingleO+index+1, new)
//                }else{
//                    Pair(index, regex)
//                }
//
//            }
//        }
//        throw Exception("The parenthesis tag must be closed")
//    }

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

    private fun normalizeStack(regexString: String) : RegexExpression {
        var currentLeft : RegexExpression? = null
        var i = 0

        while(i < regexString.length) {
            val char = regexString[i]
            val letter = char.toString()
            when {
                singleOperators.contains(letter) -> {
                    require(currentLeft != null)
                    val concat = ConcatNode()
                    val operatorRegex = SingleOperatorNodeFactory.create(letter)
                    concat.setNode(currentLeft)
                    concat.setNode(operatorRegex)
                    currentLeft = operatorRegex
                }
                operators.contains(letter) -> {
                    // Cant be the first node in the tree
                    require(currentLeft != null)
                    //Cant be the last
                    require(i < regexString.length -1)

                    val operatorNode = OperatorNodeFactory.create(letter)
                    //Set left node
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


    fun build() : RegexExpression {
        return normalize(regexString)
    }


}