package cocol

import algos.direct.DirectFromRegexNfdBuilder
import algos.direct.DirectFromRegexState
import algos.direct.DirectFromRegexTokenDetector
import algos.direct.DirectRegexSimplified
import automatons.NFD
import cocol.characters.CocolCharacterReader
import cocol.characters.CocolTextValueGetter
import cocol.ignore.CocoIgnoreReader
import cocol.tokens.CocolTokenReader
import dregex.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

import kotlinx.serialization.modules.*
import tokens.CoCoBoiCharacterSet
import tokens.TokenExpression
import tokens.TokenExpressionType
import tokens.TokenMatches
import utils.CocolLangIdents
import java.io.File
import java.lang.Exception
import java.nio.file.Paths

class CocolReader(
    val cocoNFD: NFD<DirectRegexSimplified>,
//    private val fileName: String = "CoCol.atg"
    val cocolFile: File
) {
    lateinit var name: String

    private var currentScope:CocolReaderScope = CocolReaderScope.None
    val keywordsScope = listOf(
        charactersKeyWord.ident,
        keyKeyWord.ident,
        ignoreKeyWord.ident,
        tokensKeyWord.ident
    )

    private val detector = DirectFromRegexTokenDetector(
        cocoNFD
    )

    val takenIdents = mutableListOf<String>()
    private val characters = mutableListOf<CoCoBoiCharacterSet>()
    private val keywords = mutableListOf<TokenExpression>()
    private val tokens = mutableListOf<TokenExpression>()
    private var ignore: TokenExpression? = null

    private val foundIdents = mutableListOf<String>()
    private val absPath = Paths.get("").toAbsolutePath().toAbsolutePath()
    private val basePath = "$absPath/src/main/kotlin"

    private fun getTokens(): MutableList<TokenMatches> {
//        val cocolFile = File("$basePath/$fileName")
        val content = cocolFile.readText()
        return detector.getTokens(content)
    }

    private fun checkIdentNotTaken(identToken: TokenMatches){
        if (identToken.token.ident != "ident"){
            throw Exception("El token ${identToken.match} debería ser un id para la declaración pero se encontro un ${identToken.token.ident}")
        }
        if (foundIdents.any { it == identToken.match }){
            throw Exception("El id ${identToken.match} ha sido ya tomado")
        }
    }

    fun read(){
        val tokens = getTokens()
        if (tokens.isEmpty()){
            throw Exception("El archivo esta vacio")
        }
        if (tokens.first().token.ident.toLowerCase() != "compiler"){
            throw Exception("El archivo debería iniciar con compiler")
        }
        if(tokens.count() < 2 || tokens[1].token.ident.toLowerCase() != "ident"){
            throw Exception("No se dio un nombre para el lenguaje definido")
        }
        //Set the name
        name = tokens[1].match
        //Check slice is safe
        if (tokens.count() < 4){
            throw Exception("No se encontraron suficientes tokens para cumplir con un archivo válido")
        }

        val lastToken = tokens.last()
        //Check name is valid at end
        if (lastToken.token.ident.toLowerCase() != "ident"){
            throw Exception("El ultimo token encontrado debe de ser el nombre del archivo definido")
        }else if (lastToken.match != name){
            throw Exception("El último token se encontro con el valor de ${lastToken.match} y deberia ser congruente con $name")
        }

        val endToken = tokens[tokens.count() -2]
        //If it is not end
        if (endToken.token.ident.toLowerCase() != "end"){
            throw Exception("El penultimo último token encontrado no es un END debe de ser END.")
        }

        //Slice the array
        val tokensToSearch = tokens.subList(2, tokens.count()-2)

        if (tokensToSearch.isEmpty()){
            throw Exception("No hay una definición en el archivo")
        }
        val tokensSearchIterator = tokensToSearch.iterator()

        while(tokensSearchIterator.hasNext()) {
            //If it is ignore
            if (currentScope == CocolReaderScope.Ignore){
                if (currentScope.consumed){
                    throw Exception("El scope actual ya ha sido consumido anteriormente.")
                }
                ignore = CocoIgnoreReader(
                    tokensSearchIterator,
                    characters
                ).read()
                //Proceed to next iteration
                continue
            }
            val token = tokensSearchIterator.next()
            if (token.token.ident.lowercase() == "comment"){
                continue
            }
            //Check if new scope is going to be setted
            else if (isNewScope(token)){
                setNewScope(token)
            }else {
                // Check if there is no scope
                if (currentScope == CocolReaderScope.None){
                    throw Exception("El token encontrado no se encuentra en ni uno de los posibles scopes: \n${keywordsScope.joinToString(", ")}")
                }
                //Check ident has not been taken
                checkIdentNotTaken(token)
                //Check current scope has not been consumed
                if (currentScope.consumed){
                    throw Exception("El scope actual ya ha sido consumido anteriormente.")
                }
                //Here we will check the scope
                when(currentScope){
                    CocolReaderScope.Characters -> {
                        val coCoBoiCharacterSet = findCharacter(tokensSearchIterator, token)
                        characters.add(coCoBoiCharacterSet)
                    }
                    CocolReaderScope.Keywords -> {
                        if (tokensSearchIterator.hasNext().not())
                            throw Exception("Asignación esperada para el keyword ${token.match}")
                        val assign = tokensSearchIterator.next()
                        if (assign.token.ident != assignKeyWord.ident)
                            throw Exception("Se esperaba = despues de ${token.match} se encontro ${assign.match}")
                        if (tokensSearchIterator.hasNext().not())
                            throw Exception("Se esperaba un valor despues de = para ${token.match}")
                        val value = tokensSearchIterator.next()

                        if (value.token.ident.lowercase() != "string"
                            && value.token.ident.lowercase() != CocolLangIdents.Character.ident){
                            throw Exception("Los keywords solo pueden ser strings o un char.")
                        }

                        if (tokensSearchIterator.hasNext().not())
                            throw Exception("La declaración de el keword ${token.match} se tiene que terminar con .")
                        val final = tokensSearchIterator.next()
                        val operator = try {
                          CocolOperatorClass.fromString(final.match)
                        }catch (e: Exception){
                            null
                        }

                        if (operator != CocolOperatorClass.Finish){
                            throw Exception("Se espera . para poder cerrar la declaración del keyword")
                        }

                        val keyword = TokenExpression(
                            token.match,
                            //Normalize

//                            Constants.kotlinScriptManger.eval(value.match)
//                                .toString()

                            StringNormalizer(CocolTextValueGetter(value.match).get()).normalize()
                                .map { "\\${it.toString()}" }
                                .joinToString(""),
                            TokenExpressionType.Keyword
                        )

                        keywords.add(keyword)
                    }
                    CocolReaderScope.Tokens -> {
                        val tokenFound = CocolTokenReader(
                            tokensSearchIterator,
                            token,
                            characters,
                            this.tokens
                        ).read()
                        this.tokens.add(tokenFound)
                    }
                    else -> {
                        throw Exception("Saber que paso aca")
                    }
                }
                //Add new ident
                takenIdents.add(token.match)

            }
        }




        val resultNfd = DirectFromRegexNfdBuilder(
            this.tokens,
            keywords,
            ignore
        ).buildNfd()

        val result = Json.encodeToString(resultNfd)
        val templateFile = File("$basePath/template.kt")
        val templateContent = templateFile.readText()
        val newFileContent = templateContent.replace("token_nfd_template_to_replace", result)
        val new = File("src/main/kotlin/${this.name}.kt")
        if (new.exists()){
            new.delete()
            new.createNewFile()
        }else{
            new.createNewFile()
        }
        new.writeText(newFileContent)

        println("Se ha creado el archivo $name.kt con éxito!!")
    }

    private fun findCharacter(
        tokensSearchIterator: MutableIterator<TokenMatches>,
        identToken: TokenMatches
    ) = CocolCharacterReader(
            tokensSearchIterator,
            identToken,
            characters
        ).read()




    private fun isNewScope(tokenMatch: TokenMatches): Boolean {
        val tokenIdent = tokenMatch.token.ident
        return keywordsScope.contains(tokenIdent)
    }

    private fun setNewScope(tokenMatch: TokenMatches){
        currentScope.consumed = true
        currentScope = CocolReaderScope.getScopeFromIdent(tokenMatch.token.ident)
    }
}