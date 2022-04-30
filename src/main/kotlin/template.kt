import algos.direct.DirectFromRegexNfdBuilder
import algos.direct.DirectFromRegexTokenDetector
import algos.direct.DirectRegexSimplified
import automatons.NFD
import cocol.ignore
import cocol.keywords
import cocol.tokens
import extension.requiredInput
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tokens.TokenExpression
import java.io.File
import java.nio.file.Paths

fun main(args: Array<String>){

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

    val nfd: NFD<DirectRegexSimplified> = Json.decodeFromString("""
        token_nfd_template_to_replace
    """)

    val detector = DirectFromRegexTokenDetector(
        nfd
    )
    try {
        val tokens = detector.getTokens(file.readText())
        tokens.forEach { tokenMatch ->
            println("Token found ${tokenMatch.toString()}")
        }
    }catch (e: Exception){
        println("Se ecnontro el siguiente error: ")
        println(e.localizedMessage)
    }






}