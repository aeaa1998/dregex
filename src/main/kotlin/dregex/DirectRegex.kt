package dregex

/**
 * Direct from regex node it is a concat node but we will show it as expression + # to show it more nicely
 * @constructor
 * @param expression [RegexExpression]
 *
 */
class DirectRegex(
    expression: RegexExpression
) : ConcatNode(expression.expression+"#"){
    init {
        //We set as the left the expression
        left = expression
        //Set as the right node a # node
        right = BruteForceEndNode()
    }
}