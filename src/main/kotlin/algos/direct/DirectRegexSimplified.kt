package algos.direct

import automatons.State
import kotlinx.serialization.Serializable

@Serializable
class DirectRegexSimplified(
    private val _id: String,
    private val _secondaryId: String,
) : State(_id, _secondaryId)