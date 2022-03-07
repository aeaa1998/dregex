package extension

fun String.allUnique(): Boolean = all(hashSetOf<Char>()::add)
