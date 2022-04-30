package automatons

import kotlinx.serialization.Serializable
import utils.Identifiable
import java.util.*
import kotlin.collections.HashMap


@Serializable
open class State (
    override val id: String = StateProvider.id.toString(),
    open val secondaryId: String = StateProvider.id.toString()
) : Identifiable<String> {

    init {
        StateProvider.id+=1
    }
}