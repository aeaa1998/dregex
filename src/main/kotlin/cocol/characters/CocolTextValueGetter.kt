package cocol.characters

class CocolTextValueGetter(
    private val value: String
) {
    fun get() : String {
        return if (value.startsWith("'") || value.startsWith("\"")){
            value.substring(1, value.count()-1)
        }else{
            value.substring(4, value.count()-1).toInt().toChar().toString()
        }
    }
}