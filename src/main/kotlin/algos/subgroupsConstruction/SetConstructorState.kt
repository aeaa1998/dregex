package algos.subgroupsConstruction

import automatons.State
import extension.reduceIds
import utils.Identifiable


class SetConstructorState(
    val values: List<State>
) :
    State()
{
    var marked = false
    override val id: String
    get() = values.reduceIds()
}