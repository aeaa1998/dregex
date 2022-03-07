package dregex

enum class RegexSingleOperators {
    ZeroOrMore, OneOrMore, OneOrZero;

    companion object {
        fun getFromValue(value: String) : RegexSingleOperators {
            return when(value) {
                "*" -> ZeroOrMore
                "+" -> OneOrMore
                else -> OneOrZero
            }
        }
    }
}

enum class RegexOperators {
    Or, Concat;

    companion object {
        fun getFromValue(value: String) : RegexOperators {
            return when(value) {
                "•" -> Concat
                else -> Or
            }
        }
    }
}