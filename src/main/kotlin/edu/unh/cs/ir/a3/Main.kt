package edu.unh.cs.ir.a3

import tools.TokenAnalyzer
import edu.unh.cs.ir.tools.*
import edu.unh.cs.treccar.read_data.DeserializeData
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.util.QueryBuilder
import java.io.FileInputStream
import java.io.FileWriter

fun main(args: Array<String>) {
    println("edu.unh.cs.ir.a3 main running...")

    val luceneDefaultResults: FileWriter
    val termFrequencyResults: FileWriter
    val resultsFile: String
    val qRelFile: String


    try {
        if (args[0] == "-init") {
            println("expecting first argument to be paragraph data file path...")
            println("expecting second argument to be outline data file path...")
            println("Initializing the results files from paragraph file ${args[1]} and outline file ${args[2]}")
            println("Saving results at ${System.getProperty("user.dir")}")
            luceneDefaultResults = FileWriter(System.getProperty("user.dir") + "luceneDefault.results")
            if (args[3] == "-bnnbnn")
                termFrequencyResults = FileWriter(System.getProperty("user.dir") + "bnnbnn.results")
            else if (args[3] == "-ancapc")
                termFrequencyResults = FileWriter(System.getProperty("user.dir") + "ancapc.results")
            else
                termFrequencyResults = FileWriter(System.getProperty("user.dir") + "lncltn.results")
            generateResults(luceneDefaultResults, termFrequencyResults, args)
        } else if (args[0] == "-eval") {
            println("expecting first argument to be the qrels file path...")
            println("expecting second argument to be the results file path...")
            println("Evaluating the results files from ${args[2]} with qrels file ${args[1]}")
            resultsFile = args[1]
            qRelFile = args[2]
            performEvaluation(resultsFile, qRelFile)
        }
    } catch (e: NoSuchFileException) {
        System.err.println(e.stackTrace)
        System.err.println(e.message)
        System.err.println("Requires all arguments to be used!")
        println("usage:")
        println("-init [paragraphFilePath] [outlinesFilePath] | to generate results")
        println("-eval [qRelFilePath] [resultsFileFromInitPath] to evaluate the results")
    }
}

fun generateResults(luceneDefaultResults: FileWriter, termFrequencyResults: FileWriter, args: Array<String>) {
    // Create an indexer
    val indexer = Indexer()

    // Get paragraphs from the CBOR file
    val paragraphStream = FileInputStream(args[1])
    val pageStream = FileInputStream(args[2])

    val invertedIndex = InvertedIndex()

    var idfIndexer : Indexer

    val bnnbnn = BNNBNN()
    val ancapc = ANCAPC()
    val lncltn = LNCLTN()

    if (args[3] == "-bnnbnn") {
        idfIndexer = Indexer(bnnbnn)
    }
    else if (args[3] == "-ancapc") {
        idfIndexer = Indexer(ancapc)
    }
    else {
        idfIndexer = Indexer(lncltn)
    }

    println("indexing...")

    // docId for the current document being indexed
    var docId = 0
    // Add the paragraphs to the index
    DeserializeData.iterableParagraphs(paragraphStream).forEach{
        indexer.indexParagraph(it)
        idfIndexer.indexParagraph(it)
        TokenAnalyzer.tokenizeString(StandardAnalyzer(), it.textOnly).forEach { token ->
            invertedIndex.addToIndex(token, docId)
        }
        // Calculate the doc vectors
        if (args[3] == "-bnnbnn") {
            bnnbnn.calculateDocumentVector(invertedIndex, docId)
        }
        else if (args[3] == "-ancapc") {
            ancapc.calculateDocumentVector(invertedIndex, docId)
        }
        else {
            lncltn.calculateDocumentVector(invertedIndex, docId)
        }
        docId++
    }

    println("finished indexing.")

    // Close after we load the entries
    indexer.closeIndex()
    idfIndexer.closeIndex()

    // Create the search engine
    val directory = indexer.indexDir
    val searchEngine = SearchEngine(directory)

    val idfDirectory = idfIndexer.indexDir
    val idfSearchEngine : SearchEngine

    if (args[3] == "-bnnbnn") {
        idfSearchEngine = SearchEngine(idfDirectory, bnnbnn)
    }
    else if (args[3] == "-ancapc") {
        idfSearchEngine = SearchEngine(idfDirectory, ancapc)
    }
    else {
        idfSearchEngine = SearchEngine(idfDirectory, lncltn)
    }

    // Make the query build tool
    val parser = QueryParser(IndexerFields.CONTENT.toString().toLowerCase(), StandardAnalyzer())

    println("running queries...")

    idfSearchEngine.maxDocId = docId
    searchEngine.maxDocId = docId

    // Use the pages as the query
    DeserializeData.iterableAnnotations(pageStream).forEachIndexed { query, page ->
        val tokenizedQuery = tokenizeQuery(query.toString(), StandardAnalyzer())

        // Calculate the query vectors
        if (args[3] == "-bnnbnn") {
            bnnbnn.calculateQueryVector(invertedIndex, tokenizedQuery)
        }
        else if (args[3] == "-ancapc") {
            ancapc.calculateQueryVector(invertedIndex, tokenizedQuery)
        }
        else {
            lncltn.calculateQueryVector(invertedIndex, tokenizedQuery)
        }

        performQuery(idfSearchEngine, parser, page.pageName, 100,
                listOf(page.pageId.toString(), query.toString(), "team7-${args[3]}"), termFrequencyResults)

        performQuery(searchEngine, parser, page.pageName, 100,
                listOf(page.pageId.toString(), query.toString(), "team7-luceneDefault"), luceneDefaultResults)
    }

    println("finished queries.")
}

fun tokenizeQuery(query: String, analyzer: StandardAnalyzer): ArrayList<String> {
    val tokens = ArrayList<String>()
    TokenAnalyzer.tokenizeString(analyzer, query).forEach { token ->
        tokens.add(token)
    }
    return tokens
}

fun performQuery(searchEngine: SearchEngine, parser: QueryBuilder, query: String, numResults: Int,
                 metaData: List<String>, resultsFile: FileWriter) {
    searchEngine.performPageQuery(parser.createBooleanQuery(
            IndexerFields.CONTENT.toString().toLowerCase(), query), numResults, metaData, resultsFile)
}

fun performEvaluation(resultsFile: String, qRelFile: String) {
    val evaluator = Evaluator(DataReader(resultsFile), DataReader(qRelFile))
    val rPrecisionMean = evaluator.calculateRPrecision()
    val rPrecisionError = evaluator.calculateRPrecisionError(rPrecisionMean)
    val mapMean = evaluator.calculateMeanAveragePrecision()
    val mapError = evaluator.calculateMeanAveragePrecisionError(mapMean)
    val nDCGMean = evaluator.calculateNormalizedDiscountCumulativeGain()
    val nDCGError = evaluator.calculateNormalizedDiscountCumulativeGainError(nDCGMean)
    println("RPrecision: $rPrecisionMean Error $rPrecisionError")
    println("MAP: $mapMean Error $mapError")
    println("nDCG: $nDCGMean Error $nDCGError")
}

