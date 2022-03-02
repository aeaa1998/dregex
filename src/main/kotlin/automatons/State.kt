package automatons

import utils.Identifiable
import java.util.*
import kotlin.collections.HashMap

open class State (
//    val id: String = UUID.randomUUID(),
    override val id: String = StateProvider.id.toString(),
    val secondaryId: String = StateProvider.id.toString()
) : Identifiable<String> {

    init {
        StateProvider.id+=1
    }
}