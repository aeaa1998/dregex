import cocol.*
import extension.requiredInput
import java.io.File
import java.nio.file.Paths

import java.util.*

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
        //ZERO OR MORE IS A WHILE (any of the tokens)
            CocolScanner.read(cocoNFD = AugustoCocol.augustoNFD, file)
    }catch (e: Exception){
        println("Se encontro el siguiente error: ")
        println(e.localizedMessage)
        e.printStackTrace()
    }


    return
}
