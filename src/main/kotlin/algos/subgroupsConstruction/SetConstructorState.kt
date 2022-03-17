package algos.subgroupsConstruction

import automatons.State
import extension.reduceIds
import utils.Identifiable


/**
 * This class represents the state of a set constructor algorithm graph
 * @constructor
 * @param values [List] Thompson State list representing that state
 *
 * @property marked [Boolean] Tells if state has been marked
 * @property id [String] Id represented by the current States
 */
class SetConstructorState(
    val values: List<State>
) :
    State()
{
    var marked = false
    override val id: String
    get() = values.reduceIds()
}