
import algos.direct.DirectFromRegexTokenDetector
import algos.direct.DirectRegexSimplified
import automatons.NFD
import automatons.serizalers.NFDSerializer
import cocol.productions.template.AritmeticaParser

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
        {"states":{"487":{"id":"487","secondaryId":"487","_id":"487","_secondaryId":"487"},"378":{"id":"378","secondaryId":"378","_id":"378","_secondaryId":"378"},"379":{"id":"379","secondaryId":"379","_id":"379","_secondaryId":"379"},"381":{"id":"381","secondaryId":"381","_id":"381","_secondaryId":"381"}},"initialState":{"id":"378","secondaryId":"378","_id":"378","_secondaryId":"378"},"transitions":{"487":{"0":"487","1":"487","2":"487","3":"487","4":"487","5":"487","6":"487","7":"487","8":"487","9":"487"},"378":{"\t":"379","\n":"379","\r":"379"," ":"379","(":"379",")":"379","*":"379","+":"379","-":"379",".":"379","/":"379","0":"487","1":"487","2":"487","3":"487","4":"487","5":"487","6":"487","7":"487","8":"487","9":"487",";":"379","A":"381","B":"381","C":"381","D":"381","E":"381","F":"381","G":"381","H":"381","I":"381","J":"381","K":"381","L":"381","M":"381","N":"381","O":"381","P":"381","Q":"381","R":"381","S":"381","T":"381","U":"381","V":"381","W":"381","X":"381","Y":"381","Z":"381","a":"381","b":"381","c":"381","d":"381","e":"381","f":"381","g":"381","h":"381","i":"381","j":"381","k":"381","l":"381","m":"381","n":"381","o":"381","p":"381","q":"381","r":"381","s":"381","t":"381","u":"381","v":"381","w":"381","x":"381","y":"381","z":"381"},"381":{"0":"381","1":"381","2":"381","3":"381","4":"381","5":"381","6":"381","7":"381","8":"381","9":"381","A":"381","B":"381","C":"381","D":"381","E":"381","F":"381","G":"381","H":"381","I":"381","J":"381","K":"381","L":"381","M":"381","N":"381","O":"381","P":"381","Q":"381","R":"381","S":"381","T":"381","U":"381","V":"381","W":"381","X":"381","Y":"381","Z":"381","a":"381","b":"381","c":"381","d":"381","e":"381","f":"381","g":"381","h":"381","i":"381","j":"381","k":"381","l":"381","m":"381","n":"381","o":"381","p":"381","q":"381","r":"381","s":"381","t":"381","u":"381","v":"381","w":"381","x":"381","y":"381","z":"381"}},"finalStates":[{"id":"379","secondaryId":"379","_id":"379","_secondaryId":"379"},{"id":"381","secondaryId":"381","_id":"381","_secondaryId":"381"},{"id":"487","secondaryId":"487","_id":"487","_secondaryId":"487"}],"transitionTokens":{"487":{"0":"number","1":"number","2":"number","3":"number","4":"number","5":"number","6":"number","7":"number","8":"number","9":"number"},"378":{"\t":"whitespace","\n":"whitespace","\r":"whitespace"," ":"whitespace","(":"raw (",")":"raw )","*":"raw *","+":"raw +","-":"raw -",".":"raw .","/":"raw /","0":"number","1":"number","2":"number","3":"number","4":"number","5":"number","6":"number","7":"number","8":"number","9":"number",";":"raw ;","A":"ident","B":"ident","C":"ident","D":"ident","E":"ident","F":"ident","G":"ident","H":"ident","I":"ident","J":"ident","K":"ident","L":"ident","M":"ident","N":"ident","O":"ident","P":"ident","Q":"ident","R":"ident","S":"ident","T":"ident","U":"ident","V":"ident","W":"ident","X":"ident","Y":"ident","Z":"ident","a":"ident","b":"ident","c":"ident","d":"ident","e":"ident","f":"ident","g":"ident","h":"ident","i":"ident","j":"ident","k":"ident","l":"ident","m":"ident","n":"ident","o":"ident","p":"ident","q":"ident","r":"ident","s":"ident","t":"ident","u":"ident","v":"ident","w":"ident","x":"ident","y":"ident","z":"ident"},"381":{"0":"ident","1":"ident","2":"ident","3":"ident","4":"ident","5":"ident","6":"ident","7":"ident","8":"ident","9":"ident","A":"ident","B":"ident","C":"ident","D":"ident","E":"ident","F":"ident","G":"ident","H":"ident","I":"ident","J":"ident","K":"ident","L":"ident","M":"ident","N":"ident","O":"ident","P":"ident","Q":"ident","R":"ident","S":"ident","T":"ident","U":"ident","V":"ident","W":"ident","X":"ident","Y":"ident","Z":"ident","a":"ident","b":"ident","c":"ident","d":"ident","e":"ident","f":"ident","g":"ident","h":"ident","i":"ident","j":"ident","k":"ident","l":"ident","m":"ident","n":"ident","o":"ident","p":"ident","q":"ident","r":"ident","s":"ident","t":"ident","u":"ident","v":"ident","w":"ident","x":"ident","y":"ident","z":"ident"}},"tokens":{"number":{"ident":"number","weight":1},"raw -":{"ident":"raw -","weight":5},"raw .":{"ident":"raw .","weight":3},"raw /":{"ident":"raw /","weight":7},"raw (":{"ident":"raw (","weight":8},"raw )":{"ident":"raw )","weight":9},"raw *":{"ident":"raw *","weight":6},"ident":{"ident":"ident","exceptKeywords":true},"raw +":{"ident":"raw +","weight":4},"raw ;":{"ident":"raw ;","weight":2},"whitespace":{"ident":"whitespace","type":"Ignore","weight":10}}}
    """)

    val detector = DirectFromRegexTokenDetector(
        nfd
    )


    try {
        val tokens = detector.getTokens(file.readText())
        AritmeticaParser(tokens).parse()
//        tokens.forEach { tokenMatch ->
//            println("Token found ${tokenMatch.toString()}")
//        }
    }catch (e: Exception){
        println("Se ecnontro el siguiente error: ")
        println(e.localizedMessage)
    }






}