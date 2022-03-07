package algos

import dregex.*

interface RegexVisitor {
    //Word
    fun visit(regex: WordNode)
    //Single Operators
    fun visit(regex: OneOrMoreOperatorNode)
    fun visit(regex: ZeroOrMoreOperatorNode)
    fun visit(regex: ZeroOrOne)
    //Operators
    fun visit(regex: OrOperatorNode)
    fun visit(regex: ConcatNode)

}