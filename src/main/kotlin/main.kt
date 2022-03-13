import algos.direct.DirectFromRegex
import algos.subgroupsConstruction.SetConstructor
import algos.subgroupsConstruction.SetConstructorState
import algos.thompson.Thompson
import de.vandermeer.asciitable.AsciiTable
import dregex.DRegex
import dregex.RegexExpression
import utils.Constants
import java.util.*

fun main(args: Array<String>) {
    val initial = Date().time
//    val input = "a*bc?|d(f+d*)"
    val input = "f**"
//    val input = "a(ba)+"

//    buildFromE(input)
//    buildFromE("a(b)*a")
    buildFromE(input)
//    val constructor = SetConstructor(input)
//    constructor.build()
//    val nfd = constructor.nfd
//    constructor.printDescription()
//    nfd.states.forEach { state ->
//        println("Soy el estado ${state.secondaryId} represento los estados: ")
//        (state as? SetConstructorState)?.values?.forEach { nested ->
//            println("- ${nested.id}")
//        }
//        println("Estado final son ${nfd.finalStates.map{ it.secondaryId }.joinToString(", ")}")
//        println("Estado inicial es ${nfd.initialState.secondaryId}")
//        println()
//
//    }
//    if (nfd.simulate("abaabbabaa")) {
//        println("abaabbabaa si pertenece")
//    }else{
//        println("abaabbabaa no pertenece")
//    }
    val second = Date().time
    val seconds = (second-initial) / 1000
    print("$seconds seconds passed to build nfa, nfd of $input")
}

fun buildFromE(input: String){
    val s = SetConstructor(input)
    s.build()
    s.printDescription()
    val d = DirectFromRegex(input)
    d.build()
    d.printDescription()
    printDescription(d.regexExpression)
    d.regexExpression.buildGraphFromE()


}

fun printDescription(regex: RegexExpression) {
    val at = AsciiTable()
    at.addRule()
    at.addRow("Nombre en el arbol", "Id (solo si es hoja)", "lastPos", "firstPos", "followPos")
    at.addRule()
    regex.printTable(at)
    println(at.render())
}