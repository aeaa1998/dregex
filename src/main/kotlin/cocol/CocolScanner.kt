package cocol

import algos.direct.DirectFromRegexNfdBuilder
import algos.direct.DirectFromRegexTokenDetector
import algos.direct.DirectRegexSimplified
import automatons.NFD
import automatons.serizalers.NFDSerializer
import cocol.characters.CocolCharacterReader
import cocol.characters.CocolTextValueGetter
import cocol.ignore.CocoIgnoreReader
import cocol.productions.ProductionReader
import cocol.productions.graph.ProductionFunction
import cocol.tokens.CocolTokenReader
import kotlinx.serialization.json.Json

import tokens.CoCoBoiCharacterSet
import tokens.TokenExpression
import tokens.TokenExpressionType
import tokens.TokenMatch
import utils.CocolLangIdents
import java.io.File
import java.lang.Exception
import java.nio.file.Paths

object CocolScanner {
    lateinit var name: String

    private var currentScope:CocolReaderScope = CocolReaderScope.None
    val keywordsScope = listOf(
        charactersKeyWord.ident,
        keyKeyWord.ident,
        ignoreKeyWord.ident,
        tokensKeyWord.ident,
        productionsKeyWord.ident
    )

    val takenIdents = mutableListOf<String>()
    private val characters = mutableListOf<CoCoBoiCharacterSet>()
    private val keywords = mutableListOf<TokenExpression>()
    val tokens = mutableListOf<TokenExpression>()
    private var ignore: TokenExpression? = null
    var productionRootInited = false

    private val foundIdents = mutableListOf<String>()
    private val absPath = Paths.get("").toAbsolutePath().toAbsolutePath()
    private val productions = hashMapOf<String, ProductionFunction>()
    private val basePath = "$absPath/src/main/kotlin"

    fun addRawToken(
        raw: String,
        expression: String
    ){
        if (foundIdents.any { it == raw }.not()){
            tokens.add(
                TokenExpression(
                    raw,
                    _expression = expression
                )
            )
            foundIdents.add(raw)
        }
    }

    fun addAnyToken(){
        if (foundIdents.any { it == "raw Any" }.not()){
            tokens.add(
                0,
                TokenExpression(
                    "raw Any",
                    _expression = anyCharSet.map { "\\${it}" }.joinToString("|")
                )
            )
            foundIdents.add("raw Any")
        }
    }

    private fun getTokens(
        cocoNFD: NFD<DirectRegexSimplified>,
        cocolFile: File
    ): MutableList<TokenMatch> {
        val content = cocolFile.readText()
        val detector = DirectFromRegexTokenDetector(
            cocoNFD
        )
        return detector.getTokens(content)
    }

    private fun checkIdentNotTaken(identToken: TokenMatch){
        if (identToken.token.ident != CocolLangIdents.Ident.ident){
            throw Exception("El token ${identToken.match} debería ser un id para la declaración pero se encontro un ${identToken.token.ident}")
        }
        if (foundIdents.any { it == identToken.match }){
            throw Exception("El id ${identToken.match} ha sido ya tomado")
        }
    }

    fun getFirstNotWhiteSpace(
        tokens: List<TokenMatch>,
        pointer: Int,
        action: String
    ):  Int{
        val token = tokens[pointer]
        if (token.token.ident == CocolLangIdents.WhiteSpace.ident){
            return getFirstNotWhiteSpace(tokens, if (action == "+") pointer+1 else pointer-1, action)
        }
        return pointer
    }

    fun read(cocoNFD: NFD<DirectRegexSimplified>,
             cocolFile: File){
        val tokens = getTokens(cocoNFD, cocolFile)
        if (tokens.isEmpty()){
            throw Exception("El archivo esta vacio")
        }
        val firstTokenNotWhiteSpaceIndex = getFirstNotWhiteSpace(tokens, 0, "+")
        if (tokens[firstTokenNotWhiteSpaceIndex].token.ident.lowercase() != "compiler"){
            throw Exception("El archivo debería iniciar con compiler")
        }
        val tokensCount = tokens.filter { it.token.ident !=  CocolLangIdents.WhiteSpace.ident}.count()
        val nameIndex = getFirstNotWhiteSpace(tokens, firstTokenNotWhiteSpaceIndex+1, "+")
        if(tokensCount < 2 || tokens[nameIndex].token.ident.lowercase() != CocolLangIdents.Ident.ident){
            throw Exception("No se dio un nombre para el lenguaje definido")
        }
        //Set the name
        name = tokens[nameIndex].match
        //Check slice is safe
        if (tokensCount < 4){
            throw Exception("No se encontraron suficientes tokens para cumplir con un archivo válido")
        }

        val lastTokenNotWhiteSpaceIndex = getFirstNotWhiteSpace(tokens, tokens.count()-1, "-")
        val lastToken = tokens[lastTokenNotWhiteSpaceIndex]
        //Check name is valid at end
        if (lastToken.token.ident.lowercase() != CocolLangIdents.Ident.ident){
            throw Exception("El ultimo token encontrado debe de ser el nombre del archivo definido")
        }else if (lastToken.match != name){
            throw Exception("El último token se encontro con el valor de ${lastToken.match} y deberia ser congruente con $name")
        }

        val endTokenIndex = getFirstNotWhiteSpace(tokens, lastTokenNotWhiteSpaceIndex-1, "-")
        val endToken = tokens[endTokenIndex]
        //If it is not end
        if (endToken.token.ident.lowercase() != "end"){
            throw Exception("El penultimo último token encontrado no es un END debe de ser END.")
        }

        //Slice the array
        val tokensToSearch = tokens.subList(nameIndex+1, endTokenIndex)

        if (tokensToSearch.isEmpty()){
            throw Exception("No hay una definición en el archivo")
        }
        val tokensSearchIterator = tokensToSearch.listIterator()

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
                currentScope.consumed = true
                currentScope = CocolReaderScope.None
                //Proceed to next iteration
                continue
            }
            val token = tokensSearchIterator.next()
            if (token.token.ident.lowercase() == "comment" || token.token.ident == CocolLangIdents.WhiteSpace.ident){
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
                    CocolReaderScope.Productions -> {
                        val reader = ProductionReader(
                            token,
                            tokensSearchIterator,
                            this.tokens
                        )
                        val production = reader.readProduction()
                        this.productions[production.name] = production
                        if (!productionRootInited){
                            production.root = true
                        }
                        productionRootInited = true

                    }
                    CocolReaderScope.Characters -> {
                        val coCoBoiCharacterSet = findCharacter(tokensSearchIterator, token)
                        characters.add(coCoBoiCharacterSet)
                    }
                    CocolReaderScope.Keywords -> {
                        if (tokensSearchIterator.hasNext().not())
                            throw Exception("Asignación esperada para el keyword ${token.match}")
                        var _tt = tokensSearchIterator.next()
                        //Ignore idents
                        while (_tt.token.ident == CocolLangIdents.WhiteSpace.ident && tokensSearchIterator.hasNext()){
                            _tt = tokensSearchIterator.next()
                        }
                        val assign = _tt
                        if (assign.token.ident != CocolLangIdents.Assign.ident)
                            throw Exception("Se esperaba = despues de ${token.match} se encontro ${assign.match}")
                        if (tokensSearchIterator.hasNext().not())
                            throw Exception("Se esperaba un valor despues de = para ${token.match}")

                        _tt = tokensSearchIterator.next()
                        //Ignore idents
                        while (_tt.token.ident == CocolLangIdents.WhiteSpace.ident && tokensSearchIterator.hasNext()){
                            _tt = tokensSearchIterator.next()
                        }
                        val value = _tt

                        if (value.token.ident.lowercase() != CocolLangIdents.StringIdent.ident
                            && value.token.ident.lowercase() != CocolLangIdents.Character.ident){
                            throw Exception("Los keywords solo pueden ser strings o un char.")
                        }

                        if (tokensSearchIterator.hasNext().not())
                            throw Exception("La declaración de el keword ${token.match} se tiene que terminar con .")

                        _tt = tokensSearchIterator.next()
                        //Ignore idents
                        while (_tt.token.ident == CocolLangIdents.WhiteSpace.ident && tokensSearchIterator.hasNext()){
                            _tt = tokensSearchIterator.next()
                        }

                        val final = _tt
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

        val result = Json.encodeToString(serializer = NFDSerializer(),  resultNfd)
        val templateFile = File("$basePath/template.kt")
        val templateContent = templateFile.readText()
        val newFileContentR = templateContent.replace("token_nfd_template_to_replace", result)
            .replace("//ParserClass", "${this.name}Parser")
            .replace("//import cocol.productions.template.PARSER", "import cocol.productions.template.${this.name}Parser")
        val new = File("src/main/kotlin/outputs/cocol/${this.name}Scanner.kt")
        if (new.exists()){
            new.delete()
            new.createNewFile()
        }else{
            new.createNewFile()
        }
        new.writeText(newFileContentR)
//


        //Set all computed properties
        productions.values.firstOrNull { it.root }?.computeProperties(productions)
//        productions.values.forEach { prod ->
//            prod.productionTree?.computeProperties(productions)
//        }
        val productionValues = productions.values
        //check
        val prodTemplateFile = File("$basePath/cocol/productions/template/template.kt")
        val templateProdContent = prodTemplateFile.readText()
        val newFileContent = templateProdContent
            .replace("//root_function", "${ productionValues.first { it.root }.name }()")
            .replace("//define_functions", productionValues.map { it.generateBody() }.joinToString("\n\n"))
            .replace("TemplateNameHere", "${this.name}Parser")


        val newProd = File("src/main/kotlin/outputs/cocol/${this.name}Parser.kt")
        if (newProd.exists()){
            newProd.delete()
            newProd.createNewFile()
        }else{
            newProd.createNewFile()
        }
        newProd.writeText(newFileContent)
        println("Se han creado el archivo ${name}Scanner.kt y ${name}Parse.kt con éxito!!")
    }

    private fun findCharacter(
        tokensSearchIterator: MutableIterator<TokenMatch>,
        identToken: TokenMatch
    ) = CocolCharacterReader(
            tokensSearchIterator,
            identToken,
            characters
        ).read()




    private fun isNewScope(tokenMatch: TokenMatch): Boolean {
        val tokenIdent = tokenMatch.token.ident
        return keywordsScope.contains(tokenIdent)
    }

    private fun setNewScope(tokenMatch: TokenMatch){
        currentScope.consumed = true
        currentScope = CocolReaderScope.getScopeFromIdent(tokenMatch.token.ident)
    }
}