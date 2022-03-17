package extension

import utils.Identifiable

fun requiredInput() : String {
    while(true){
        val input = readLine()
        if(input!=null && input.isNotBlank()) return input
        println("Campo obligatorio")
    }
}

fun getIntInput(text: String = "Ingrese una opción: ") : Int {
    while(true){
        print(text)
        val input = readLine()
        input?.toIntOrNull()?.let {
            return it
        }
        println("Ingrese un número válido")
    }
}

fun List<String>.getSelectedOption(): Int {
    while (true){
        forEachIndexed { index, string ->
            println("${ index + 1 } $string ")
        }
        val input = getIntInput()
        if (input < 1 || input > count()){
            println("Ingrese una opcion válida")
        }else{
            return input - 1
        }
    }
}

fun <T> List<T>.safeLast(): T? {
    if (isNotEmpty()) return last()
    else return null
}

fun <T> MutableList<T>.safeReplaceLast(item: T) {
    if (isNotEmpty()){
        val index = count() - 1
        this[index] = item
    }else{
        add(item)
    }
}

infix fun <T> MutableList<T>.plus(items: MutableList<T>): MutableList<T> {
    val mutable: MutableList<T> = mutableListOf()
    mutable.addAll(this)
    mutable.addAll(items)
    return mutable
}

fun <T> MutableList<T>.addUnique(element: T)  {
    if (!contains(element)) add(element)
}

fun <T> MutableList<T>.addAllUnique(elements: Iterable<T>)  {
    elements.forEach(::addUnique)
}

fun <T: Identifiable<R>, R> MutableList<T>.addAllUnique(element: T)  {
    val currentIds = map { it.id }
    val id = element.id
    if (!currentIds.contains(id)){
        add(element)
    }
}

fun <R> List<Identifiable<R>>.reduceIds() : String{
    val idsSorted = this.map { it.id.toString() }.sorted()
    return idsSorted.joinToString(separator = "-")
}

fun <R, J : Identifiable<R>> List<J>.containsId(element : J) : Boolean{
    val currentIds = map { it.id }
    return currentIds.contains(element.id)
}

fun <R, J : Identifiable<R>> List<J>.containsAnyId(elements : List<J>) : Boolean{
    val currentIds = map { it.id }
    elements.forEach {
        val contains = currentIds.contains(it.id)
        if (contains) return true
    }
    return false
}
