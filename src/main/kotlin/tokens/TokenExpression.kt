package tokens

import dregex.DRegex
import dregex.RegexExpression
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@Serializable
class TokenExpression(
    val ident: String,
    @Transient
    val _expression: String = "",
    val type: TokenExpressionType = TokenExpressionType.Normal
) {
    var exceptKeywords = false
    val regexExpression: RegexExpression by lazy { DRegex(_expression).build() }
    var weight = 0

    fun propagate(weight: Int){
        this.weight = weight
//        val stack = Stack<RegexExpression>()
//        stack.push(regexExpression)
//        while (!stack.isEmpty()){
//            val top = stack.pop()
//            top.left?.let { child ->
//                stack.add(child)
//            }
//            top.right?.let { child ->
//                stack.add(child)
//            }
//            top.tokenExpression = this
//        }
        regexExpression.propagate(this)
    }
}