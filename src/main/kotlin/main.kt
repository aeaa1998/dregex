import algos.direct.DirectFromRegex
import algos.direct.DirectFromRegexState
import algos.direct.DirectFromRegexTokenDetector
import algos.direct.DirectRegexSimplified
import algos.subgroupsConstruction.SetConstructor
import algos.subgroupsConstruction.SetConstructorState
import algos.thompson.Thompson
import automatons.NFA
import automatons.NFD
import automatons.State
import cocol.*
import de.vandermeer.asciitable.AsciiTable
import dregex.DRegex
import dregex.RegexExpression
import extension.getSelectedOption
import extension.requiredInput
import tokens.TokenExpression
import java.io.File
import java.nio.file.Paths

import java.util.*
import javax.script.ScriptEngineManager

fun main() {
    var fileExists = false
    lateinit var file : File
    while (fileExists.not()){
        println("Ingrese el nombre del archivo")
        val fileName: String = requiredInput()
        val absPath = Paths.get("").toAbsolutePath().toAbsolutePath()
        val basePath = "$absPath/src/main/kotlin"
        file = File("$basePath/$fileName")
        if (file.exists().not()){
            println("El archivo que ingreso no existe!")
        }else{
            fileExists = true
        }
    }
    try {
        CocolReader(
            cocoNFD = AugustoCocol.augustoNFD,
            file
        ).read()
    }catch (e: Exception){
        println("Se encontro el siguiente error: ")
        println(e.localizedMessage)
    }


    return
}
