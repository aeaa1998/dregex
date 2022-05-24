
import algos.direct.DirectFromRegexTokenDetector
import algos.direct.DirectRegexSimplified
import automatons.NFD
import automatons.serizalers.NFDSerializer
import cocol.productions.template.DoubleParser

import extension.requiredInput
import kotlinx.serialization.json.Json

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

    val nfd: NFD<DirectRegexSimplified> = Json.decodeFromString(deserializer = NFDSerializer(), """
        {"states":{"453":{"id":"453","secondaryId":"453","_id":"453","_secondaryId":"453"},"443":{"id":"443","secondaryId":"443","_id":"443","_secondaryId":"443"},"378":{"id":"378","secondaryId":"378","_id":"378","_secondaryId":"378"},"379":{"id":"379","secondaryId":"379","_id":"379","_secondaryId":"379"},"403":{"id":"403","secondaryId":"403","_id":"403","_secondaryId":"403"},"381":{"id":"381","secondaryId":"381","_id":"381","_secondaryId":"381"}},"initialState":{"id":"378","secondaryId":"378","_id":"378","_secondaryId":"378"},"transitions":{"453":{"0":"453","1":"453","2":"453","3":"453","4":"453","5":"453","6":"453","7":"453","8":"453","9":"453"},"443":{"0":"453","1":"453","2":"453","3":"453","4":"453","5":"453","6":"453","7":"453","8":"453","9":"453"},"378":{" ":"403","(":"379",")":"379","\t":"403","\n":"403","*":"379","+":"379","-":"379","\r":"403",".":"379","/":"379","0":"381","1":"381","2":"381","3":"381","4":"381","5":"381","6":"381","7":"381","8":"381","9":"381",";":"379"},"403":{" ":"403","\t":"403","\n":"403","\r":"403"},"381":{"0":"381","1":"381","2":"381","3":"381","4":"381","5":"381","6":"381","7":"381","8":"381","9":"381",".":"443"}},"finalStates":[{"id":"379","secondaryId":"379","_id":"379","_secondaryId":"379"},{"id":"381","secondaryId":"381","_id":"381","_secondaryId":"381"},{"id":"403","secondaryId":"403","_id":"403","_secondaryId":"403"},{"id":"453","secondaryId":"453","_id":"453","_secondaryId":"453"}],"transitionTokens":{"453":{"0":"decnumber","1":"decnumber","2":"decnumber","3":"decnumber","4":"decnumber","5":"decnumber","6":"decnumber","7":"decnumber","8":"decnumber","9":"decnumber"},"443":{"0":"decnumber","1":"decnumber","2":"decnumber","3":"decnumber","4":"decnumber","5":"decnumber","6":"decnumber","7":"decnumber","8":"decnumber","9":"decnumber"},"378":{" ":"white","(":"raw (",")":"raw )","\t":"white","\n":"white","*":"raw *","+":"raw +","-":"raw -","\r":"white",".":"raw .","/":"raw /","0":"number","1":"number","2":"number","3":"number","4":"number","5":"number","6":"number","7":"number","8":"number","9":"number",";":"raw ;"},"403":{" ":"white","\t":"white","\n":"white","\r":"white"},"381":{"0":"number","1":"number","2":"number","3":"number","4":"number","5":"number","6":"number","7":"number","8":"number","9":"number"}},"tokens":{"raw -":{"ident":"raw -","weight":6},"number":{"ident":"number"},"raw .":{"ident":"raw .","weight":4},"raw /":{"ident":"raw /","weight":8},"white":{"ident":"white","weight":2},"raw (":{"ident":"raw (","weight":9},"raw )":{"ident":"raw )","weight":10},"raw *":{"ident":"raw *","weight":7},"raw +":{"ident":"raw +","weight":5},"raw ;":{"ident":"raw ;","weight":3},"decnumber":{"ident":"decnumber","weight":1}}}
    """)

    val detector = DirectFromRegexTokenDetector(
        nfd
    )


    try {
        val tokens = detector.getTokens(file.readText())
        DoubleParser(tokens).parse()
//        tokens.forEach { tokenMatch ->
//            println("Token found ${tokenMatch.toString()}")
//        }
    }catch (e: Exception){
        println("Se ecnontro el siguiente error: ")
        println(e.localizedMessage)
    }






}