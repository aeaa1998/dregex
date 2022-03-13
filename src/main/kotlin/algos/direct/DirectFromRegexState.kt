package algos.direct

import automatons.State
import dregex.RegexExpression
import extension.reduceIds
import utils.Identifiable


class DirectFromRegexState(
    val values: List<RegexExpression>
) :
    State()
{
    var marked = false
    override val id: String
    get() = values.reduceIds()

}