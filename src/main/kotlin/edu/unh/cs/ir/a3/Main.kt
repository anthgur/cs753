package edu.unh.cs.ir.a3

import edu.unh.cs.ir.tools.*
import edu.unh.cs.tools.TokenizerAnalyzer
import edu.unh.cs.treccar.read_data.DeserializeData
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.search.similarities.BasicStats
import org.apache.lucene.search.similarities.Similarity
import org.apache.lucene.search.similarities.SimilarityBase
import java.io.FileInputStream
import java.io.FileWriter

fun main(args: Array<String>) {
    println("edu.unh.cs.ir.a3 main running...")

    val luceneDefaultResults: FileWriter
    val lncLtnResults: FileWriter
    val resultsFile: String
    val qRelFile: String

    try {
        when {
            args[0] == "-init" -> {
                println("expecting first argument to be paragraph data file path...")
                println("expecting second argument to be outline data file path...")
                println("Initializing the results files from paragraph file ${args[1]} and outline file ${args[2]}")
                println("Saving results at ${System.getProperty("user.dir")}")
                luceneDefaultResults = FileWriter(System.getProperty("user.dir") + "luceneDefault.results")
                lncLtnResults = FileWriter(System.getProperty("user.dir") + "lncLtnResults.results")
                generateResults(luceneDefaultResults, lncLtnResults, args)
            }
            args[0] == "-eval" -> {
                println("expecting first argument to be the qrels file path...")
                println("expecting the second argument to be the results file path...")
                println("Evaluating the results file from ${args[2]} with qrels file ${args[1]}")
                resultsFile = args[1]
                qRelFile = args[2]
                performEvaluation(resultsFile, qRelFile)
            }
            else -> {
                println("expecting first argument to be paragraph data file path...")
                println("expecting second argument to be outline data file path...")
                println("expecting third argument to be hierarchical data file path...")
                TODO("Clarify what needs to be done.")
            }
        }
    } catch (e: NoSuchFieldError) {
        println("Requires all arguments to be used!")
        println("usage:")
        println("-init [paragraphFilePath] [outlinesFilePath] | to generate results")
        println("-eval [qRelFilePath] [resultsFileFromInitPath] to evaluate the results")
        println(e.message)

    }

}

fun generateResults(luceneDefaultResults: FileWriter, lncLtnResults: FileWriter, args: Array<String>) {
    // Create an indexer for Lucene default
    val indexer = Indexer()

    // Create an index for LNC.LTN
    val lncLtnIndexer = Indexer()

    // Get paragraphs from the CBOR file
    val paragraphStream = FileInputStream(args[1])

    // Get pages from the CBOR file
    val pageStream = FileInputStream(args[2])

    // Holds our frequencies in a table representation
    val invertedIndex = InvertedIndex()

    // Standard token and stemming rules
    val analyzer = StandardAnalyzer()

    // Document ID for the current document being indexed
    var currentIndexDocID = 0

    // Document vectors
    val documentVectors = ArrayList<ArrayList<Double>>()

    // Add the paragraphs to each index
    DeserializeData.iterableParagraphs(paragraphStream).forEach {
        TokenizerAnalyzer.tokenizeString(analyzer, it.textOnly).forEach { token ->
            invertedIndex.addToIndex(token, currentIndexDocID)
        }
        documentVectors.add(invertedIndex.generateDocumentVector(TFIDF_DOC_TYPE.LNC, currentIndexDocID))
        indexer.indexParagraph(it)
        lncLtnIndexer.indexParagraph(it)
        currentIndexDocID++
    }


    // Close after we load the entries
    indexer.closeIndex()
    lncLtnIndexer.closeIndex()

    // Create the search engines
    val directory = indexer.indexDir

    val lncLtnDirectory = lncLtnIndexer.indexDir

    // Page title queries
    DeserializeData.iterableAnnotations(pageStream).forEach { page ->
        val query = page.pageName
        val pageId = page.pageId.toString()
        val tokenizedQuery = tokenizeQuery(query, analyzer)
        val queryVector = invertedIndex.generateQueryVector(tokenizedQuery, TFIDF_QUERY_TYPE.LTN)

        val ourSimilarity = customSimilarity(invertedIndex, documentVectors, queryVector)
        val searchEngine = SearchEngine(directory)
        val ltnLncEngine = SearchEngine(lncLtnDirectory, ourSimilarity)

        searchEngine.performQuery(query, 100).scoreDocs.forEachIndexed { rank, scoreDoc ->
            val doc = searchEngine.getDoc(scoreDoc.doc)
            val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
            luceneDefaultResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-luceneDefault\n")
        }
        ltnLncEngine.performQuery(query, 100).scoreDocs.forEachIndexed { rank, scoreDoc ->
            val doc = searchEngine.getDoc(scoreDoc.doc)
            val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
            lncLtnResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-lncltn\n")
        }

   }



    luceneDefaultResults.close()
    lncLtnResults.close()

}

class customSimilarity(private val invertedIndex: InvertedIndex, private val documentVectors: ArrayList<ArrayList<Double>>,
                       private val queryVector: ArrayList<Double>) : SimilarityBase() {
    var currentDocument = 0

    override fun toString(): String {
        return "LNC.LTN Similarity"
    }

    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        val normalizedDocumentVector = invertedIndex.normalizeVector(documentVectors[currentDocument],
                TFIDF_DOC_TYPE.LNC)
        val normalizedQueryVector = invertedIndex.normalizeVector(queryVector,
                TFIDF_DOC_TYPE.BNN)
        val score = calculateInnerProduct(normalizedDocumentVector, normalizedQueryVector)
        currentDocument++
        return score.toFloat()
    }

}

fun calculateInnerProduct(documentVectors: ArrayList<Double>, queryVector: ArrayList<Double>): Double {
    var innerProduct = 0.0
    assert(documentVectors.size == queryVector.size)
    documentVectors.forEachIndexed { index, d ->
        innerProduct += d + queryVector[index]
    }
    return innerProduct
}

fun tokenizeQuery(query: String, analyzer: StandardAnalyzer): ArrayList<String> {
    val tokens = ArrayList<String>()
    TokenizerAnalyzer.tokenizeString(analyzer, query).forEach { token ->
        tokens.add(token)
    }
    return tokens
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

