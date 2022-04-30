package cocol

/**
 * This enum tells in which scope the reader is currently at
 * it can bee consumed just once
 */
enum class CocolReaderScope {
    None, Characters, Tokens, Keywords, Ignore;
    //Tells if it has been consumed
    var consumed = false

    val isRequired: Boolean
    get() = when(this){
        None -> false
        Ignore -> false
        Characters -> true
        Tokens -> true
        Keywords -> true
    }

    //Get the order of each
    val order: Int
    get() = when(this){
        None -> -1
        Ignore -> 2
        Characters -> 3
        Keywords -> 4
        Tokens -> 5

    }

    companion object {
        val allIdents = mapOf(
            "ignore" to Ignore,
            "characters" to Characters,
            "keywords" to Keywords,
            "tokens" to Tokens
        )
        fun getScopeFromIdent(ident: String): CocolReaderScope {
            return allIdents[ident.toLowerCase()] ?: None
        }
    }

}