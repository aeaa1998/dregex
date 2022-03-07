package extension

import utils.Identifiable


fun <T> List<T>.safeLast(): T? {
    if (isNotEmpty()) return last()
    else return null
}

fun <T> MutableList<T>.safeReplaceLast(item: T): Unit {
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
