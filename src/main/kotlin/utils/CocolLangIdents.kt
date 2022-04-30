package utils

enum class CocolLangIdents {
    Character, Operator;

    val ident :String
    get() = when(this){
        Character -> "character"
        Operator -> "operator"
    }
}