package extension

import tokens.TokenMatch
import utils.CocolLangIdents

fun getNextTokenIsNotWhiteSpace(tokensSearchIterator: ListIterator<TokenMatch>): TokenMatch {
    var _tt = tokensSearchIterator.next()
    while (_tt.token.ident == CocolLangIdents.WhiteSpace.ident && tokensSearchIterator.hasNext()){
        _tt = tokensSearchIterator.next()
    }
    return _tt
}