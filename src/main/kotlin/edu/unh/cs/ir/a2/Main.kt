package edu.unh.cs.ir.a2

import edu.unh.cs.treccar.read_data.DeserializeData
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.similarities.BasicStats
import org.apache.lucene.search.similarities.SimilarityBase
import org.apache.lucene.util.QueryBuilder
import java.io.FileInputStream
import edu.unh.cs.ir.tools.*
import java.io.FileReader
import java.io.FileWriter

fun main(args: Array<String>) {
    println("edu.unh.cs.ir.a2 main running...")

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
            termFrequencyResults = FileWriter(System.getProperty("user.dir") + "termFrequency.results")
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

fun performQuery(searchEngine: SearchEngine, parser: QueryBuilder, query: String, numResults: Int,
                 metaData: List<String>, resultsFile: FileWriter) {
    searchEngine.performPageQuery(parser.createBooleanQuery(
            IndexerFields.CONTENT.toString().toLowerCase(), query), numResults, metaData, resultsFile)
}

fun generateResults(luceneDefaultResults: FileWriter, termFrequencyResults: FileWriter, args: Array<String>) {
    class freqSimilarity : SimilarityBase() {
        override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
            return stats!!.totalTermFreq.toFloat()
        }

        override fun toString(): String {
            return "Frequency Similarity based on sum #{q_i}"
        }
    }

    // Instance of the term frequency similarity
    val termFrequencySimilarity = freqSimilarity()

    // Create an indexer
    val indexer = Indexer()

    // Create term frequency indexer
    val termFrequencyIndexer = Indexer(termFrequencySimilarity)

    // Get paragraphs from the CBOR file
    val paragraphStream = FileInputStream(args[1])
    val pageStream = FileInputStream( args[2])

    // Add the paragraphs to the index
    DeserializeData.iterableParagraphs(paragraphStream).forEach{
        indexer.indexParagraph(it)
        termFrequencyIndexer.indexParagraph(it)
    }

    // Close after we load the entries
    indexer.closeIndex()
    termFrequencyIndexer.closeIndex()

    // Create the analyzer for the search engine
    val analyzer = StandardAnalyzer()

    // Create the search engine
    val directory = indexer.indexDir
    val searchEngine = SearchEngine(directory)

    // Create the term frequency search engine
    val termFrequencyDirectory = termFrequencyIndexer.indexDir
    val termFrequencySearchEngine = SearchEngine(termFrequencyDirectory, termFrequencySimilarity)

    // Make the query build tool
    val parser = QueryParser(IndexerFields.CONTENT.toString().toLowerCase(), analyzer)

    // Use the pages as the query
    DeserializeData.iterableAnnotations(pageStream).forEachIndexed { query, page ->
        performQuery(searchEngine, parser, page.pageName, 100,
                listOf(page.pageId.toString(), query.toString(), "team7-luceneDefault"), luceneDefaultResults)
        performQuery(termFrequencySearchEngine, parser, page.pageName, 100,
                listOf(page.pageId.toString(), query.toString(), "team7-termFrequency"), termFrequencyResults)
    }

    //val evaluator = Evaluator()

    searchEngine.closeSearchEngine()
    termFrequencySearchEngine.closeSearchEngine()

    termFrequencyResults.close()
    luceneDefaultResults.close()
}

fun performEvaluation(resultsFile: String, qRelFile: String) {
    val evaluator = Evaluator(DataReader(resultsFile), DataReader(qRelFile))
    println("RPrecision: ${evaluator.calculateRPrecision()}")
}

