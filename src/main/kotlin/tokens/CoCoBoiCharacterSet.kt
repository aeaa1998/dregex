package tokens


class CoCoBoiCharacterSet(
    val ident: String,
    val value: List<String>,
)
{
    lateinit var representation : String
    lateinit var representationList : List<String>

    fun produce(){
        this.representationList = value
        this.representation = value.map { char ->
            "\\$char"
        }
            .joinToString("|")
    }

    fun generateRepresentation(characterSets: List<CoCoBoiCharacterSet> = listOf()) : String {
        if (this::representation.isInitialized){
            return representation
        }
        var copy = value.joinToString("")
        characterSets.forEach { set ->
            set.generateRepresentation(characterSets)
            copy = set.representationList.map { copy.replace(set.ident, it) }.joinToString { it }
        }

        representationList = copy.split("").subList(1, copy.count()+1)

        representation  = representationList.joinToString("|")

//            .map { v ->
//            if (v.startsWith("\"")){
//                v.split("").joinToString { "|" }
//            }else{
//                characterSets[v]
//            }
//        }

        return representation
    }
}