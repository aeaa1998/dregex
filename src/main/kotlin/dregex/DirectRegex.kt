package dregex

import kotlinx.serialization.Serializable

/**
 * Direct from regex node it is a concat node but we will show it as expression + # to show it more nicely
 * @constructor
 * @param expression [RegexExpression]
 *
 */

class DirectRegex(
    private val normalExpression: RegexExpression
) : ConcatNode(normalExpression.expression+"#"){
    init {
        //We set as the left the expression
        left = normalExpression
        //Set as the right node a # node
        right = BruteForceEndNode()
    }
}