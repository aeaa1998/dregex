package utils

enum class CocolLangIdents {
    Character, Operator, CharactersScope, Productions,
    Any, Ident, ActionParamBlockStart, ActionParamBlockEnd, SemanticRule, WildCard,
    Assign, StringIdent, Grouper, WhiteSpace;

    val ident : String
    get() = when(this){
        WhiteSpace -> "whitespace"
        Character -> "character"
        Operator -> "operator"
        CharactersScope -> "characters"
        Productions -> "productions"
        Any -> "any"
        Ident -> "ident"
        ActionParamBlockStart -> "action-param-block-start"
        ActionParamBlockEnd -> "action-param-block-end"
        SemanticRule -> "action-block"
        WildCard -> "wildcard"
        Assign -> "assign"
        StringIdent -> "string"
        Grouper -> "grouper"
    }
}