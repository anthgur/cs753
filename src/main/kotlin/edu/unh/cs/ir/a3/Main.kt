package edu.unh.cs.ir.a3

import edu.unh.cs.ir.tools.*
import edu.unh.cs.tools.TokenizerAnalyzer
import edu.unh.cs.treccar.read_data.DeserializeData
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.search.similarities.BasicStats
import org.apache.lucene.search.similarities.SimilarityBase
import java.io.FileInputStream
import java.io.FileWriter

fun main(args: Array<String>) {
    println("edu.unh.cs.ir.a3 main running...")

    val luceneDefaultResults: FileWriter
    val customResults: FileWriter
    val resultsFile: String
    val qRelFile: String

    try {
        when {
            args[0] == "-init" -> {
                println("expecting first argument to be paragraph data file path...")
                println("expecting second argument to be outline data file path...")
                println("Initializing the results files from paragraph file ${args[1]} and outline file ${args[2]}")
                println("Saving results at ${System.getProperty("user.dir")}")
                println("Running the ${args[3]} model.")
                luceneDefaultResults = FileWriter(System.getProperty("user.dir") + "luceneDefault.results")
                customResults = when {
                    args[3] == "lncltn" -> FileWriter(System.getProperty("user.dir") + "lncLtn.results")
                    args[3] == "bnnbnn" -> FileWriter(System.getProperty("user.dir") + "bnnBnn.results")
                    args[3] == "ancapc" -> FileWriter(System.getProperty("user.dir") + "ancApc.results")
                    else -> FileWriter(System.getProperty("user.dir") + "custom.results")
                }
                generateResults(luceneDefaultResults, customResults, args)
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
        println("-init [paragraphFilePath] [outlinesFilePath] [model] | to generate results")
        println("-eval [qRelFilePath] [resultsFileFromInitPath] | to evaluate the results")
        println(e.message)

    }

}

fun generateResults(luceneDefaultResults: FileWriter, customResults: FileWriter, args: Array<String>) {
    // Create an indexer for Lucene default
    val indexer = Indexer()

    // Create an index for LNC.LTN
    val lncLtnIndexer = Indexer()

    // Create an index for BNN.BNN
    val bnnBnnIndexer = Indexer()

    // Create an index for ANC.APC
    val ancApcIndexer = Indexer()

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
    val documentVectorsLnc = ArrayList<ArrayList<Double>>()
    val documentVectorsBnn = ArrayList<ArrayList<Double>>()
    val documentVectorsAnc = ArrayList<ArrayList<Double>>()

    val model = args[3]

    // Add the paragraphs to each index
    DeserializeData.iterableParagraphs(paragraphStream).forEach {
        TokenizerAnalyzer.tokenizeString(analyzer, it.textOnly).forEach { token ->
            invertedIndex.addToIndex(token, currentIndexDocID)
        }
        when (model) {
            "lncltn" -> {
                documentVectorsLnc.add(invertedIndex.generateDocumentVector(TFIDF_DOC_TYPE.LNC, currentIndexDocID))
                lncLtnIndexer.indexParagraph(it)
            }
            "bnnbnn" -> {
                documentVectorsBnn.add(invertedIndex.generateDocumentVector(TFIDF_DOC_TYPE.BNN, currentIndexDocID))
                bnnBnnIndexer.indexParagraph(it)
            }
            "ancapc" -> {
                documentVectorsAnc.add(invertedIndex.generateDocumentVector(TFIDF_DOC_TYPE.ANC, currentIndexDocID))
                ancApcIndexer.indexParagraph(it)
            }
        }
        indexer.indexParagraph(it)
        currentIndexDocID++
    }

    println("Indexing complete.")


    // Close after we load the entries
    indexer.closeIndex()
    lncLtnIndexer.closeIndex()
    bnnBnnIndexer.closeIndex()
    ancApcIndexer.closeIndex()

    // Create the search engines
    val directory = indexer.indexDir

    val lncLtnDirectory = lncLtnIndexer.indexDir
    val bnnBnnDirectory = bnnBnnIndexer.indexDir
    val ancApcDirectory = ancApcIndexer.indexDir

//    println(currentIndexDocID)

    val maxDocID = currentIndexDocID

    // Page title queries
    DeserializeData.iterableAnnotations(pageStream).forEach { page ->
        val query = page.pageName
        val pageId = page.pageId.toString()
        val tokenizedQuery = tokenizeQuery(query, analyzer)

        val queryVectorLtn = invertedIndex.generateQueryVector(tokenizedQuery, TFIDF_QUERY_TYPE.LTN)
        val queryVectorBnn = invertedIndex.generateQueryVector(tokenizedQuery, TFIDF_QUERY_TYPE.BNN)
        val queryVectorApc = invertedIndex.generateQueryVector(tokenizedQuery, TFIDF_QUERY_TYPE.APC)

        val lncLtnSim = lncLtnSimilarity(invertedIndex, documentVectorsLnc, queryVectorLtn)
        val bnnBnnSim = bnnBnnSimilarity(invertedIndex, documentVectorsBnn, queryVectorBnn)
        val ancApcSim = ancApcSimilarity(invertedIndex, documentVectorsAnc, queryVectorApc)

        val searchEngine = SearchEngine(directory)
        val ltnLncEngine = SearchEngine(lncLtnDirectory, lncLtnSim)
        val bnnBnnEngine = SearchEngine(bnnBnnDirectory, bnnBnnSim)
        val ancApcEngine = SearchEngine(ancApcDirectory, ancApcSim)

        print("Starting Lucene default results...")
        val topDefaultScoredDocuments = searchEngine.performQuery(query, 100)

        topDefaultScoredDocuments.scoreDocs.forEachIndexed { rank, scoreDoc ->
            if (scoreDoc.doc <= maxDocID) {
                val doc = searchEngine.getDoc(scoreDoc.doc)
                val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
                luceneDefaultResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-luceneDefault\n")
            }
        }
        println("Lucene default results done.")

        when (model) {
            "lncltn" -> {
                print("Starting LNC.LTN results...")

                val topLncLtnScoredDocuments = ltnLncEngine.performQuery(query, 100)

                topLncLtnScoredDocuments.scoreDocs.forEachIndexed { rank, scoreDoc ->
                    if (scoreDoc.doc <= maxDocID) {
                        val doc = ltnLncEngine.getDoc(scoreDoc.doc)
                        val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
                        customResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-lncltn\n")
                    }
                }
                println("LNC.LTN results done.")
            }
            "bnnbnn" -> {
                print("Starting BNN.BNN results...")

                val topBnnBnnScoredDocuments = bnnBnnEngine.performQuery(query, 100)

                topBnnBnnScoredDocuments.scoreDocs.forEachIndexed { rank, scoreDoc ->
                    if (scoreDoc.doc <= maxDocID) {
                        val doc = bnnBnnEngine.getDoc(scoreDoc.doc)
                        val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
                        customResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-bnnbnn\n")
                    }
                }
                println("BNN.BNN results done.")
            }
            "ancapc" -> {
                print("Starting ANC.APC results...")

                val topAncApcScoredDocuments = ancApcEngine.performQuery(query, 100)

                topAncApcScoredDocuments.scoreDocs.forEachIndexed { rank, scoreDoc ->
                    if (scoreDoc.doc <= maxDocID) {
                        val doc = ancApcEngine.getDoc(scoreDoc.doc)
                        val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
                        customResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-ancapc\n")
                    }
                }

                println("ANC.APC results done.")
            }
        }

    }

    luceneDefaultResults.close()
    customResults.close()

}

class lncLtnSimilarity(private val invertedIndex: InvertedIndex, private val documentVectors: ArrayList<ArrayList<Double>>,
                       private val queryVector: ArrayList<Double>) : SimilarityBase() {
    private var currentDocument = 0

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

class bnnBnnSimilarity(private val invertedIndex: InvertedIndex, private val documentVectors: ArrayList<ArrayList<Double>>,
                       private val queryVector: ArrayList<Double>) : SimilarityBase() {
    private var currentDocument = 0

    override fun toString(): String {
        return "LNC.LTN Similarity"
    }

    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        val normalizedDocumentVector = invertedIndex.normalizeVector(documentVectors[currentDocument],
                TFIDF_DOC_TYPE.BNN)
        val normalizedQueryVector = invertedIndex.normalizeVector(queryVector,
                TFIDF_DOC_TYPE.BNN)
        val score = calculateInnerProduct(normalizedDocumentVector, normalizedQueryVector)
        currentDocument++
        return score.toFloat()
    }

}

class ancApcSimilarity(private val invertedIndex: InvertedIndex, private val documentVectors: ArrayList<ArrayList<Double>>,
                       private val queryVector: ArrayList<Double>) : SimilarityBase() {
    private var currentDocument = 0

    override fun toString(): String {
        return "LNC.LTN Similarity"
    }

    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        val normalizedDocumentVector = invertedIndex.normalizeVector(documentVectors[currentDocument],
                TFIDF_DOC_TYPE.ANC)
        val normalizedQueryVector = invertedIndex.normalizeVector(queryVector,
                TFIDF_DOC_TYPE.ANC)
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

fun performRankEvaluation(resultsFile: String, lucenResultsFile: String) {
    val evaluator = SpearmanRank(DataReader(resultsFile), DataReader(resultsFile))
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

