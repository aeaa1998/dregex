package algos.direct

import automatons.State
import dregex.RegexExpression
import extension.reduceIds

/**
 * This class represents the state of a direct from regex graph
 * @constructor
 * @param values [List] Regex expression list representing that state
 *
 * @property marked [Boolean] Tells if state has been marked
 * @property id [String] Id represented by the current regex expressions
 */


class DirectFromRegexState(
    val values: List<RegexExpression>
) :
    State()
{
    var marked = false
    override val id: String = values.reduceIds()

    fun simplified() : DirectRegexSimplified{
        return  DirectRegexSimplified(
            secondaryId,
            secondaryId
        )
    }
}