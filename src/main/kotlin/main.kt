import algos.direct.DirectFromRegex
import algos.subgroupsConstruction.SetConstructor
import algos.thompson.Thompson
import dregex.DRegex
import java.util.*

fun main(args: Array<String>) {
    val initial = Date().time
    val input = "a*bc?|d(f+d*)"
//    SetConstructor(input).build()
    DirectFromRegex(input).build()
    val second = Date().time
    val seconds = (second-initial) / 1000
    print("$seconds seconds passed to build nfa, nfd of $input")
}