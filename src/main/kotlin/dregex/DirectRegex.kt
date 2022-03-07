package dregex

class DirectRegex(
    expression: RegexExpression
) : ConcatNode(){
    init {
        left = expression
        right = BruteForceEndNode()
    }
}