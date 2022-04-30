package cocol

import java.lang.Exception

enum class CocolOperatorClass {
    Minus, Plus, Or, Except, Finish, Range;
//    BracketStart, BracketEnd, ParenthesisStart, ParenthesisEnd, BraceStart, BraceEnd,

    companion object  {
        val charactersScopePrecedence = mapOf(
            Minus to 1,
            Plus to 1,
            Range to 2
        )

        val ignoreScopePrecedence = mapOf(
            Minus to 1,
            Plus to 1,
            Range to 2
        )

        fun isOperator(value:String) : Boolean {
            return try {
                fromString(value)
                return true
            }catch (e:Exception){
                false
            }
        }

        fun fromString(value: String): CocolOperatorClass{
            return when(value.toLowerCase()){
                "-" -> Minus
                "+" -> Plus
                "|" -> Or
                "except keywords" -> Except
                "." -> Finish
                ".." -> Range
                else -> {
                    throw Exception("Invalid operator")
                }
            }
        }
    }
}