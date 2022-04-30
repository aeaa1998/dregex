package cocol

import algos.direct.DirectFromRegexNfdBuilder
import algos.direct.DirectFromRegexState
import algos.direct.DirectFromRegexTokenDetector
import algos.direct.DirectRegexSimplified
import automatons.NFD
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import tokens.CoCoBoiCharacterSet
import tokens.TokenExpression
import tokens.TokenExpressionType
import utils.CocolLangIdents
import java.io.File
import java.nio.file.Paths

//Space char 32
//Tab 9
//Line tab 11
val extendedAscii = 7.toChar().rangeTo(256.toChar())
val regexKeys = listOf('(', ')', '|', '}', '{', '[', ']', '+', '?', '*', '\\')
val anyCharSet = extendedAscii.map { it.toString() }.joinToString("")
val letter = CoCoBoiCharacterSet("letter", "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray().map { it.toString() })
val digit = CoCoBoiCharacterSet("digit", "0123456789".toCharArray().map { it.toString() })

val whiteSpaces = TokenExpression(
    "whitespace",
    "${32.toChar()}|${ 9.toChar() }|${ 11.toChar() }| |\n|${13.toChar()}",
    TokenExpressionType.Ignore
)

val ignoreKeyWord = TokenExpression(
    "ignore",
    "IGNORE",
    TokenExpressionType.Keyword
)

val charactersKeyWord = TokenExpression(
    "characters",
    "CHARACTERS",
    TokenExpressionType.Keyword
)

val tokensKeyWord = TokenExpression(
    "tokens",
    "TOKENS",
    TokenExpressionType.Keyword
)

val compilerKeyWord = TokenExpression(
    "compiler",
    "COMPILER",
    TokenExpressionType.Keyword
)

val endKeyWord = TokenExpression(
    "end",
    "END",
    TokenExpressionType.Keyword
)

val keyKeyWord = TokenExpression(
    "keywords",
    "KEYWORDS",
    TokenExpressionType.Keyword
)

val assignKeyWord = TokenExpression(
    "assign",
    "=",
    TokenExpressionType.Keyword
)

val operatorKeyWord = TokenExpression(
    "operator",
    "\\-|\\+|\\||(EXCEPT KEYWORDS)|.|(..)",
    TokenExpressionType.Keyword
)

val grouperKeyWord = TokenExpression(
    "grouper",
    "\\{|\\}|\\(|\\)|\\[|\\]",
    TokenExpressionType.Keyword
)

val charToken = TokenExpression(
    CocolLangIdents.Character.ident,
    "(CHR\\({1|2|3|4|5|6|7|8|9|0}\\))|('${
        extendedAscii
            .filter { it != '\'' }
            .map {
                if (regexKeys.contains(it)){
                    return@map "\\${it.toString()}"
                }
                it.toString()
            }

            .joinToString("')|('")}')"
)


val comment = TokenExpression(
    "comment",
    "\\(\\.{${extendedAscii
        .filter { it != '.' && it != ')'}
        .map {
            if (regexKeys.contains(it)){
                return@map "\\${it.toString()}"
            }
            it.toString()
        }
        .joinToString("|")}}\\.\\)"
)

val string = TokenExpression(
    "string",
    "\\\"{${extendedAscii
        .filter { it != '"' }
        .map {
            if (regexKeys.contains(it)){
                return@map "\\${it.toString()}"
            }
            it.toString()
        }
        .joinToString("|")}|${8212.toChar().toString()}}\\\""
)

val number = TokenExpression(
    "number",
    "(${digit.generateRepresentation()}){(${digit.generateRepresentation()})}"
)

val ident = TokenExpression(
    "ident",
    "(${letter.generateRepresentation()}){(${letter.generateRepresentation()})|(${digit.generateRepresentation()})|\\_}"
).apply {
    this.exceptKeywords = true
}

val any = TokenExpression(
    "any",
    "ANY"
)

val tokens = listOf(
    string,
    number,
    ident,
    charToken,
//    commentStart,
    comment,
    any
)

val keywords = listOf(
    grouperKeyWord,
    charactersKeyWord,
    tokensKeyWord,
    compilerKeyWord,
    endKeyWord,
    assignKeyWord,
    operatorKeyWord,
    keyKeyWord,
    ignoreKeyWord,
)

val ignore = listOf(
    whiteSpaces
)

fun generateCocolDefinition() {
    val nfdBuilder = DirectFromRegexNfdBuilder(
        tokens,
        keywords,
        ignore[0]
    )
    val nfd = nfdBuilder.buildNfd()
    val result = Json.encodeToString(nfd)
    val absPath = Paths.get("").toAbsolutePath().toAbsolutePath()
    val basePath = "$absPath/src/main/kotlin"
    val templateFile = File("$basePath/AugustoCocol")
    val templateContent = templateFile.readText()
    val newFileContent = templateContent.replace("augusto_nfd", result)
    val new = File("src/main/kotlin/AugustoCocol.kt")
    if (new.exists()){
        new.delete()
        new.createNewFile()
    }else{
        new.createNewFile()
    }
    new.writeText(newFileContent)
}

fun main(){
    generateCocolDefinition()
}


