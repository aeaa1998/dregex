package cocol

import utils.Constants

class StringNormalizer(
    private val _initial: String
) {
    companion object {

        private fun resolveEscapedChar(char: Char): Char {
            return when(char){
                '\'' -> '\''
                '\\' -> '\\'
                '"' -> '"'
                'n' -> '\n'
                't' -> '\t'
                'b' -> '\b'
                else -> throw Exception("El caracter escapado no es válido")
            }
        }
    }
    fun normalize(): String {
        val stringsList = mutableListOf<String>()

        val _initial = _initial
        var isEscapingCharFound = false
        _initial.forEachIndexed { index, char ->
            if (isEscapingCharFound){
                //If the escaped char in the string is not valid throw an error
                val escaped = resolveEscapedChar(char)
                isEscapingCharFound = false
                stringsList.add(escaped.toString())
            }
            else if (char == '\\'){
                isEscapingCharFound = true
            }else{
                stringsList.add(char.toString())
            }

        }
        return stringsList.joinToString("")
    }
}