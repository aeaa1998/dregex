import algos.direct.DirectFromRegex
import algos.direct.DirectFromRegexState
import algos.subgroupsConstruction.SetConstructor
import algos.subgroupsConstruction.SetConstructorState
import algos.thompson.Thompson
import automatons.NFA
import automatons.NFD
import automatons.State
import de.vandermeer.asciitable.AsciiTable
import dregex.DRegex
import dregex.RegexExpression
import extension.getSelectedOption
import extension.requiredInput
import utils.Constants
import java.util.*

fun main() {

    var option = 0
    val options = listOf("Mostrar automatas", "Simular todos", "Salir")
    val secondOptions = listOf("Todos", "Mostrar solo thompson", "Mostrar conjuntos", "Mostrar directo")
    while (option != 2){
        option = options.getSelectedOption()
        when (option){
            //Automatons graphs
            0 -> {
                print("Ingrese la expresión regular a calcular: ")
                val regexInput = requiredInput().trim()
                when(secondOptions.getSelectedOption()) {
                    //All
                    0 -> {
                        runSafeBlock {
                            SetConstructor(regexInput).build()
                            DirectFromRegex(regexInput).build()
                        }
                    }
                    //Thompson
                    1 -> {
                        runSafeBlock {
                            Thompson(regexInput).build()
                        }
                    }
                    //Set Constructor
                    2 -> {
                        runSafeBlock {
                            SetConstructor(regexInput).build()
                        }
                    }
                    //Set Constructor
                    3 -> {
                        runSafeBlock {
                            DirectFromRegex(regexInput).build()
                        }
                    }
                }
            }

            //Simulate table
            1 -> {
                print("Ingrese la expresión regular para calcular los automatas: ")
                val regexInput = requiredInput().trim()
                runSafeBlock {
                    val thompson = Thompson(regexInput).build().nfa
                    val constructor = SetConstructor(regexInput).build().nfd
                    val direct = DirectFromRegex(regexInput).build().nfd
                    print("Ingrese el texto a evaluar: ")
                    val textToEvaluate = requiredInput()
                    getSimulations(
                        textToEvaluate,
                        thompson,
                        constructor,
                        direct
                    )
                }
            }
            else -> println("Adios :)")
        }

    }
}

fun runSafeBlock(block: () -> Unit){
    try {
        block()
    }catch (e: Exception){
        println("Se encontro el problema:\n${e.localizedMessage}\n\n")
    }
}

fun getTimeElapsed(block: () -> Unit): Long{
    val initial = Date().time
    block()
    val second = Date().time
    val seconds = (second-initial)
    return seconds * 1000
}

fun getSimulations(
    input: String,
    thompson: NFA<State>,
    constructor: NFD<SetConstructorState>,
    direct: NFD<DirectFromRegexState>
) {
    val at = AsciiTable()
    at.addRule()
    at.addRow("Nombre del algoritmo", "Tiempo", "Paso?")
    at.addRule()
    var passed = false
    val thompsonTime = getTimeElapsed {
        passed = thompson.simulate(input)
    }

    at.addRow("Thompson", "$thompsonTime milisegundos", if (passed) "Si" else "No")

    val constructorTime = getTimeElapsed {
        passed = constructor.simulate(input)
    }

    at.addRow("Conjuntos", "$constructorTime milisegundos", if (passed) "Si" else "No")

    val directTime = getTimeElapsed {
        passed = direct.simulate(input)
    }

    at.addRow("Directo", "$directTime milisegundos", if (passed) "Si" else "No")
    at.addRule()
    println(at.render())
    println()
}



fun printDescription(regex: RegexExpression) {
    val at = AsciiTable()
    at.addRule()
    at.addRow("Nombre en el arbol", "Id (solo si es hoja)", "lastPos", "firstPos", "followPos")
    at.addRule()
    regex.printTable(at)
    println(at.render())
}