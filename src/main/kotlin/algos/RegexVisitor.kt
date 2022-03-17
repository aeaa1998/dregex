package algos

import dregex.*

/**
 * Interface to allow to visit a regex tree
 * it is intended to do different actions depending on the node visited
 */
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